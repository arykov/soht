/******************************************************************************
 * $Source: /cvsroot/telnetoverhttp/clients/java/src/java/soht/client/java/configuration/ConfigurationManager.java,v $
 * $Revision: 1.6 $
 * $Author: edaugherty $
 * $Date: 2004/06/29 21:08:52 $
 ******************************************************************************
 * Copyright (c) 2003, Eric Daugherty (http://www.ericdaugherty.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Eric Daugherty nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 * *****************************************************************************
 * For current versions and more information, please visit:
 * http://www.ericdaugherty.com/dev/soht
 *
 * or contact the author at:
 * soht@ericdaugherty.com
 *****************************************************************************/

package soht.client.java.configuration;

import org.apache.log4j.PropertyConfigurator;

import java.util.List;
import java.util.Properties;
import java.util.Enumeration;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import javax.net.ssl.*;

/**
 * Handles the configuration data for the SOHT Proxy client.
 *
 * @author Eric Daugherty
 */
public class ConfigurationManager
{
    //***************************************************************
    // Constants
    //***************************************************************

	public static enum ConnectionModes{
		STATELESS, STATELESS_TEXT, STATEFUL;
	}

    //***************************************************************
    // Variables
    //***************************************************************

    private Properties properties;
    private String propertiesFile;

    private String serverURL;
    private boolean disableHostNameVerification;
    private boolean serverLoginRequired;
    private String serverUsername;
    private String serverPassword;
    private ConnectionModes connectionMode;
    private boolean socksServerEnabled;
    private int socksServerPort;
    
    private List hosts;

    //***************************************************************
    // Constructor
    //***************************************************************

    /**
     * Initializes a new ConfigurationManager instance to load/save
     * configuration information to/from the specified properties file.
     *
     * @param propertiesFile the path/filename to the soht properties file.
     * @throws ConfigurationException thrown if there is an error loading the file.
     */
    public ConfigurationManager( String propertiesFile ) throws ConfigurationException
    {
        this.propertiesFile = propertiesFile;
        loadProperties();

        initializeLogger();
    }

    //***************************************************************
    // Parameter Access Methods
    //***************************************************************

    public String getServerURL()
    {
        return serverURL;
    }

    public void setServerURL( String serverURL )
    {
        this.serverURL = serverURL;
    }

    public boolean isServerLoginRequired()
    {
        return serverLoginRequired;
    }

	/**
	 * @return Returns the socksServerPort.
	 */
	public int getSocksServerPort() {
		return socksServerPort;
	}
	
	/**
	 * @param socksServerPort The socksServerPort to set.
	 */
	public void setSocksServerPort(int socksServerPort) {
		this.socksServerPort = socksServerPort;
	}
	
	/**
	 * @return Returns the socksServerEnabled.
	 */
	public boolean isSocksServerEnabled() {
		return socksServerEnabled;
	}
	
	/**
	 * @param socksServerEnabled The socksServerEnabled to set.
	 */
	public void setSocksServerEnabled(boolean socksServerEnabled) {
		this.socksServerEnabled = socksServerEnabled;
	}
	
    public void setServerLoginRequired( boolean serverLoginRequired )
    {
        this.serverLoginRequired = serverLoginRequired;
    }

    public String getServerUsername()
    {
        return serverUsername;
    }

    public void setServerUsername( String serverUsername )
    {
        this.serverUsername = serverUsername;
    }

    public String getServerPassword()
    {
        return serverPassword;
    }

    public void setServerPassword( String serverPassword )
    {
        this.serverPassword = serverPassword;
    }

    public ConnectionModes getConnectionMode() {
        return connectionMode;
    }



    public List getHosts()
    {
        return hosts;
    }

    public void setHosts( List hosts )
    {
        this.hosts = hosts;
    }


    //***************************************************************
    // Public Helper Methods
    //***************************************************************

    /**
     * Initializes and configures a HttpURLConnection for use.  This includes
     * setting the server URL and any proxy configuration, if neccessary.
     *
     * @return a configured HttpURLConnection
     * @throws IOException thrown if unable to connect to URL
     */
    public HttpURLConnection getURLConnection() throws IOException
    {
        String url_str = getServerURL();
        URL url = new URL( url_str );

        HttpURLConnection urlConnection;

        if (url_str.toLowerCase().startsWith("https"))
        {
         // https connection
         HttpsURLConnection urlSConnection = (HttpsURLConnection) url.openConnection();

         if(disableHostNameVerification) {
        	 // from : http://www.kickjava.com/?http://www.kickjava.com/1930.htm
        	 urlSConnection.setHostnameVerifier(new HostnameVerifier()
        	 {
        		 public boolean verify(String hostname, SSLSession session)
        		 {
        			 // I don't care if the certificate doesn't match host name
        			 return true;
        		 }
        	 });
         }

         urlConnection = urlSConnection;
        }
        else
         // non-https connection
         urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod( "POST" );



        urlConnection.setDoOutput(true);

        

        return urlConnection;
    }

    //***************************************************************
    // Private Helper Methods
    //***************************************************************

    /**
     * Loads configuration information from the configured properties file.
     *
     * @throws ConfigurationException thrown if there is an error loading the file.
     */
    private void loadProperties() throws ConfigurationException
    {
        // Load the file.
        properties = new Properties();
        try
        {
            properties.load( new FileInputStream( propertiesFile ) );
        }
        catch( Throwable throwable )
        {
            throw new ConfigurationException( "Unable to load configuration file: " + propertiesFile + " - " + throwable.toString() );
        }

        // Load server properties
        serverURL = getRequiredProperty( "server.url" );
        disableHostNameVerification = Boolean.parseBoolean(properties.getProperty("tls.disableHostnameVerification", "false"));
        String serverLoginRequiredString = properties.getProperty( "server.loginrequired", "false" );
        serverLoginRequired = Boolean.valueOf( serverLoginRequiredString ).booleanValue();
        // Load the server username/password if a login is required.
        // they are both required if a login is required
        if( serverLoginRequired )
        {
            serverUsername = getRequiredProperty( "server.username" );
            serverPassword = getRequiredProperty( "server.password" );
        }

        // Load the connection mode.
        connectionMode = ConnectionModes.valueOf(properties.getProperty( "server.mode", ConnectionModes.STATEFUL.toString()));

        // Load SOCKS server settings
        String socksServerEnabledString = properties.getProperty("socks.server.enabled", "false");
        socksServerEnabled = Boolean.valueOf(socksServerEnabledString).booleanValue();
        if (socksServerEnabled) {
        	String socksServerPortString = properties.getProperty("socks.server.port", "1080");
        	socksServerPort = Integer.parseInt(socksServerPortString);
        }
        
        // Load mappings
        hosts = new ArrayList();
        Enumeration propertyKeys = properties.keys();
        String keyName;
        String keyValue;
        int delimiterIndex;
        String localPort;
        String remoteHost;
        String remotePort;
        while( propertyKeys.hasMoreElements() )
        {
            keyName = (String) propertyKeys.nextElement();
            if( keyName.startsWith( "port." ) )
            {
                localPort = keyName.substring( 5 );
                keyValue = properties.getProperty( keyName );

                delimiterIndex = keyValue.indexOf( ":" );
                if( delimiterIndex == -1 )
                {
                    throw new ConfigurationException( "Mapping for local port: " + localPort + " invalid.  Please specify value as <host>:<port>." );
                }
                remoteHost = keyValue.substring( 0, delimiterIndex );
                remotePort = keyValue.substring( delimiterIndex + 1 );

                hosts.add( new Host( localPort, remoteHost, remotePort ) );
            }
        }
    }

    /**
     * Loads the specified property, and throws a ConfigurationException
     * if the property does not exist.
     *
     * @param propertyName the property key to load.
     * @return the property value.  This will never be null.
     * @throws ConfigurationException thrown if the property is null.
     */
    private String getRequiredProperty( String propertyName ) throws ConfigurationException
    {
        String property = properties.getProperty( propertyName );
        if( property == null )
        {
            throw new ConfigurationException( "Missing required property: " + propertyName );
        }
        return property;
    }

    /**
     * Configure Logging framework.
     */
    private void initializeLogger()
    {
        PropertyConfigurator.configureAndWatch( propertiesFile );
    }
}

/******************************************************************************
 * $Source: /cvsroot/telnetoverhttp/servers/java/src/java/soht/server/java/ConnectionInfo.java,v $
 * $Revision: 1.2 $
 * $Author: edaugherty $
 * $Date: 2003/09/09 20:06:45 $
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

package soht.server.java;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Stores all information for a specific connection.
 * <p>
 * Instances of this class are stored in the SessionContext.
 *
 * @author Eric Daugherty
 */
public class ConnectionInfo {

    //***************************************************************
    // Variables
    //***************************************************************

    private static long nextConnectionId = 0;

    
    private long connectionId;
    private String clientHost;
    private String targetHost;
    private String targetPort;
    private Socket socket;
    private Date timeOpened;
    private InputStream inputStream;
    private OutputStream outputStream;

    //***************************************************************
    // Constructor
    //***************************************************************

    public ConnectionInfo( String clientHost, String targetHost,
                           String targetPort, Socket socket, Date timeOpened,
                           InputStream inputStream, OutputStream outputStream )
    {
        synchronized( ConnectionInfo.class )
        {
            connectionId = nextConnectionId++;
        }
        
        this.clientHost = clientHost;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.socket = socket;
        this.timeOpened = timeOpened;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    //***************************************************************
    // Parameter Access Methods
    //***************************************************************

    public long getConnectionId()
    {
        return connectionId;
    }

    
    public String getClientHost()
    {
        return clientHost;
    }

    public String getTargetHost()
    {
        return targetHost;
    }

    public String getTargetPort()
    {
        return targetPort;
    }

    public Socket getSocket()
    {
        return socket;
    }

    public Date getTimeOpened()
    {
        return timeOpened;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    //***************************************************************
    // Public Methods
    //***************************************************************

    /**
     * Returns a string containing the clientHost, targetHost, and targetPort.
     */
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "MM/dd/yyyy hh:mm:ss a zzz" );
        return "Connection from: " + clientHost + " to " + targetHost + ":" + targetPort + " Opened: " + dateFormat.format( timeOpened );
    }
}
//EOF

/******************************************************************************
 * $Source: /cvsroot/telnetoverhttp/servers/java/src/java/soht/server/java/SocketProxyServlet.java,v $
 * $Revision: 1.4 $
 * $Author: edaugherty $
 * $Date: 2003/11/26 16:30:50 $
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

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;


/**
 * Handles communcation betwen client and proxy.
 * Implements the IO functionality for the Socket over HTTP
 * proxy service.  This servlet accepts incoming HTTP connections,
 * and opens the requested socket session and proxies the IO over
 * the HTTP connection.
 *
 * @author Eric Daugherty
 */
public class SocketProxyServlet extends AbstractHandler
{
    //***************************************************************
    // Variables
    //***************************************************************

    private static Logger log = Logger.getLogger( SocketProxyServlet.class );

    private static ConnectionManager connectionManager = new ConnectionManager();
    private static int readerCount = 0;
    private static int writerCount = 0;
    private int blockSize = 4;

    //***************************************************************
    // Parameter Access Methods
    //***************************************************************

    public static ConnectionManager getConnectionManager()
    {
        return connectionManager;
    }

    public static int getReaderCount()
    {
        return readerCount;
    }

    public static int getWriterCount()
    {
        return writerCount;
    }

    public void handle(String arg0, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    		throws IOException, ServletException {

    	if("POST".equals(request.getMethod()))doPost(httpRequest, httpResponse);
    	else if("GET".equals(request.getMethod()))doGet(httpRequest, httpResponse);
    	request.setHandled(true);
    	
    }
    //***************************************************************
    // HttpServlet Methods
    //***************************************************************

    /**
     * Handles GET requests.  We want to ignore these requests because all
     * clients use the POST method.  This will return a blank page so that
     * anyone who snoopes around will not get any information about the
     * services offered.
     *
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @throws java.io.IOException thrown if there is an error writing the output.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = response.getWriter();

        out.println( "<HTML>" );
        out.println( "<BODY>" );
        out.println( "</BODY>" );
        out.println( "</HTML>" );
    }

    /**
     * Handles POST reqeusts.  Clients post a request here to initiate a
     * session or to read, write or close an existing session.
     *
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @throws IOException thrown if there is an error writing the output.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //Extract the action paramter.
        String action = request.getParameter( "action" );

        //If it is a new session, that means we need to open a new connection.
        if( action != null && action.equals( "open" ) ) {

            log.info( "Open connection request received." );

            open( request, response );
        }
        //Otherwise, we need to perform some action on the existing session
        else {

            long id = Long.parseLong( request.getParameter( "id" ) );

            //CLOSE
            if( action != null && action.equals( "close" ) ) {
                log.info( "Close connection request recieved." );

                connectionManager.removeConnection( id );
            }
            //READ
            else if( action != null && action.equals( "read" ) ) {
                log.debug( "Read request recieved." );
                read( id, response, true, false );
            }
            //WRITE
            else if( action != null && action.equals( "write" ) ) {
                log.debug( "Write request recieved." );
                write( id, request );
            }
            else if( action != null && action.equals( "readwrite" ) ) {
                log.debug( "Read/Write request recieved." );
                readWrite( id, request, response, false );
            }
            else if( action != null && action.equals( "readwritetext" ) ) {
                log.debug( "Read/Write request recieved." );
                readWrite( id, request, response, true );
            }
        }
    }

    //***************************************************************
    // Private Methods
    //***************************************************************

    /**
     * Handles all connection setup for open connection requests.
     */
    private void open( HttpServletRequest request, HttpServletResponse response ) throws IOException {

        PrintWriter out = new PrintWriter( response.getWriter() );

        try {

            String host = request.getParameter( "host" );
            String port = request.getParameter( "port" );
            if( host == null || port == null ) {
                throw new Exception( "Host or port parameter not specified." );
            }

            // Open the connection.
            Socket socket = new Socket( InetAddress.getByName( host ), Integer.parseInt( port ) );

            // Capture the information about the connection.
            ConnectionInfo connectionInfo = new ConnectionInfo(                    
                    request.getRemoteHost(),
                    host,
                    port,
                    socket,
                    new Date(),
                    socket.getInputStream(),
                    socket.getOutputStream() );

            // Add the connection to the manager.
            connectionManager.addConnection( connectionInfo );

            log.info( "Connection Opened: " + connectionInfo.toString() );

            // Let the client know the connection was opened successfully.
            out.println("SUCCESS");
            out.println( connectionInfo.getConnectionId() );
        }
        catch( Throwable e ) {
            log.error( "Error processing request for new connection. " + e.getMessage() );

            // Let the client know the connection failed.
            out.println("FAIL - " + e.getMessage() );
        }
    }

    /**
     * Handles a READ request from the client.  Data is read from the
     * proxied connection and written to the client.
     *
     * @param id the proxied connection id.
     * @param response the connection to the client.
     * @param block true if the thread should block while waiting for data.
     */
    private void read( long id, HttpServletResponse response, boolean block, boolean encode ) {

        byte[] bytes = new byte[1024 * blockSize];
        int count;

        if( log.isDebugEnabled() ) log.debug( "Read Buffer Size: " + bytes.length );

        try {

            //Add to the count of "Reader Threads"
            addReader();

            //Open the output stream to write to the client, and the input stream to read
            //from the proxied telnet connection.
            OutputStream out = response.getOutputStream();
            response.setContentType(encode?"text/plain":"application/octet-stream");
            response.setHeader("Content-Type", encode?"text/plain":"application/octet-stream");
            OutputStream responseOut = out;
            if(encode) {
            	out = Base64.getUrlEncoder().wrap(out);
            	
            }
            ConnectionInfo info = connectionManager.getConnection( id );

            if( info == null ) {
                log.error( "Client requested a connection that was closed (connectionId:" + id + ")." );
                out.write( 0 );
                out.close();
                return;
            }

            InputStream in = info.getInputStream();

            // If we are not blocking, set the correct SoTimeout.
            if( !block ) {
                info.getSocket().setSoTimeout( 100 );
            }
            else {
                info.getSocket().setSoTimeout( 0 );
            }

            boolean isFirst = true;
            while( true ) {

                count = 0;

                // Only block for a read if we are in blocking mode.
                if( block )
                {
                    //Read data from the proxied telnet session.
                    count = in.read( bytes );
                }
                else {
                    try {
                        count = in.read( bytes );
                    }
                    catch (SocketTimeoutException timeoutException ) {
                        // This is normal, this just means that no data was
                        // ready to be read.
                    }
                }

                //A count of -1 indicates that the inputstream has been closed.
                if ( count == -1 ) {
                    out.write( 0 );
                    close(out, responseOut);
                    
                    connectionManager.removeConnection( info.getConnectionId() );
                    log.info( "Removing connection because the remote server closed the connection." );
                    break;
                }

                // Log the actual bytes read/written.
                if( log.isDebugEnabled() ) {
                    log.debug( "Client read " + count + " bytes.");
                    StringBuffer debugOut = new StringBuffer( "Data: " );
                    for( int index = 0; index < count; index++ ) {
                        debugOut.append( (int)bytes[index] );
                        debugOut.append( "," );
                    }
                    log.debug( debugOut );
                }

                //Write the data to the HTTP client.
                if( isFirst ) {
                    out.write( 1 );
                    isFirst = false;
                }
                out.write( bytes, 0, count );
                // If we are not in blocking mode, break out of the loop.
                if( !block ) {
                	close(out, responseOut);;
                    break;
                }

                //this causes chunked transfer encoding
                out.flush();

            }
        }
        catch( IOException ioe ) {
            //This just means the connection was closed.  This is fine.
        }
        catch( Throwable t ) {
            log.error( "Non IO Error occured while reading from the proxied telnet connection. " + t.getMessage(), t );
        }
        finally {
            //This "ReaderThread" is ending, so remove it from the count.
            removeReader();
            if( block ) {
                connectionManager.removeConnection( id );
            }
        }
    }

    private void close(OutputStream ... oss) {
		for(OutputStream os:oss) {
			try {
				os.close();
			}catch(Exception ex) {
				
			}
		}
		
	}

	/**
     * Processes a WRITE request from the client and writes the data sent from
     * the client to the proxied connection.
     *
     * @param id the proxied connection id.
     * @param request used to read the WRITE data from the client.
     */
    private void write( long id, HttpServletRequest request ) {

        try {
            addWriter();

            ConnectionInfo info = connectionManager.getConnection( id );
            if( info != null ) {

                OutputStream socketOut = info.getOutputStream();

                // Read the Data and decode it into bytes.
                String data = request.getParameter( "data" );
                int dataLength = Integer.parseInt( request.getParameter( "datalength" ) );
                byte[] decodedBytes = decode( data, dataLength );

                if( log.isDebugEnabled() ) {
                    log.debug( "Client wrote " + dataLength + " bytes." );
                    StringBuffer debugOut = new StringBuffer( "Data: " );
                    for( int index = 0; index < decodedBytes.length; index++ ) {
                        debugOut.append( (int)decodedBytes[index] );
                        debugOut.append( "," );
                    }
                    log.debug( debugOut );
                }

                socketOut.write( decodedBytes );
            }
            else {
                log.info( "Write method attempted to write to a closed connection" );
            }
        }
        catch( IOException ioe ) {
            //This just means the connection was closed.  This is fine.
        }
        catch( Throwable t ) {
            connectionManager.removeConnection( id );
            log.error( "Non IO Error occured while writing to the proxied telnet connection. " + t.getMessage(), t );
        }
        finally {
            removeWriter();
        }
    }

    /**
     * Processes a READ/WRITE request from the client.  Writes the data sent from
     * the client to the proxied connection and reads data from the proxied connection
     * and writes it to the client.
     *
     * @param id the proxied connection id.
     * @param request used to read the WRITE data from the client.
     * @param response used to write the READ data to the client.
     */
    private void readWrite( long id, HttpServletRequest request, HttpServletResponse response, boolean encode ) {

        // Do the WRITE part.
        write( id, request );
        read( id, response, false, encode );
    }




    /**
     * Converts the data payload into a byte array.  See the
     * <a href='http://www.ericdaugherty.com/dev/soht/protocol.html'>
     * Protocol definition</a> for more details.
     *
     * @param inputData encoded String
     * @param decodedLength the number of 'real' bytes sent.
     * @return byte array.
     */
    private byte[] decode( String inputData, int decodedLength )
    {
        byte[] rawBytes = inputData.getBytes();
        byte[] decodedBytes = new byte[decodedLength];
        int rawIndex = 0;
        char character;
        int decodedInt;
        for( int decodedIndex = 0; decodedIndex < decodedLength; decodedIndex++ )
        {
            character = (char) rawBytes[rawIndex];
            if( character == '#' )
            {
                decodedInt = Integer.decode( "#" + (char) rawBytes[rawIndex + 1] + (char) rawBytes[rawIndex + 2] ).intValue();
                decodedBytes[decodedIndex] = (byte) decodedInt;
                rawIndex = rawIndex + 3;
            }
            else
            {
                decodedBytes[decodedIndex] = rawBytes[rawIndex];
                rawIndex++;
            }
        }

        return decodedBytes;
    }

    /**
     * Provides synchronized access to the total number of readers count.
     */
    private static synchronized void addReader() {
        readerCount++;
    }

    /**
     * Provides synchronized access to the total number of readers count.
     */
    private static synchronized void removeReader() {
        readerCount--;
    }

    /**
     * Provides synchronized access to the total number of writers count.
     */
    private static synchronized void addWriter() {
        writerCount++;
    }

    /**
     * Provides synchronized access to the total number of writers count.
     */
    private static synchronized void removeWriter() {
        writerCount--;
    }

    public static void main(String[] args) throws Exception{
    	int port = 80;
    	try {
    		port = Integer.parseInt(args[0]);
    		System.out.println(String.format("Will bind to port %s", port));;
    	}catch(Exception ex) {
    		System.out.println("Will bind to port 80. In the future you could pass port as a parameter.");;
    	}
		Server server = new Server(port);
		server.setHandler(new SocketProxyServlet());
		server.start();
		server.dumpStdErr();
		server.join();

	}
}



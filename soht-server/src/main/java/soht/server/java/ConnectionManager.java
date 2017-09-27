/******************************************************************************
 * $Source: /cvsroot/telnetoverhttp/servers/java/src/java/soht/server/java/ConnectionManager.java,v $
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

import java.util.*;

import soht.server.java.ConnectionInfo;

/**
 * Handles and caches all open connections.
 *
 * @author Eric Daugherty
 */
public class ConnectionManager {

    /**
     * Adds a new connection to the manager.
     */
    public void addConnection( ConnectionInfo connectionInfo ) {
        _connections.put( new Long( connectionInfo.getConnectionId() ), connectionInfo );
    }

    /**
     * Returns the ConnectionInfo object for the specified connection.
     * <p>
     * Returns null if the connection is not cached.
     */
    public ConnectionInfo getConnection( long connectionId ) {
        return (ConnectionInfo) _connections.get( new Long( connectionId ) );
    }

    /**
     * Closes the input and output streams of the specified connection
     * and removes it from the cache.
     */
    public void removeConnection( long connectionId ) {

        ConnectionInfo info = getConnection( connectionId );

        try {
            info.getInputStream().close();
        }
        catch( Exception e ) {
            //Nothing to do here.
        }

        try {
            info.getOutputStream().close();
        }
        catch( Exception e ) {
            //Nothing to do here.
        }

        _connections.remove( new Long( connectionId ) );
    }

    /**
     * Returns the number of active connections.
     *
     * @return number of active connections.
     */
    public int getConnectionCount()
    {
        return _connections.size();
    }

    /**
     * Returns an iterator of all the active connections.
     *
     * @return iterator of ConnectionInfo instances.
     */
    public Iterator getConnections()
    {
        return _connections.values().iterator();
    }

    //***************************************************************
    // Variables
    //***************************************************************

    Map _connections = Collections.synchronizedMap( new HashMap() );

}
//EOF

SOHT (Socket over HTTP Tunneling)
http://www.ericdaugherty.com/dev/soht

For full Installation and Usage details, please refer to:
http://www.ericdaugherty.com/dev/soht/javaserver.html

Installation

This distribution contains a .WAR file that may be deployed in any
J2EE compliant Servlet container.  If you wish to use all the default
values, you can simply deploy the WAR and begin using it.

The server requires a properties file to function properly.  Be default,
the server will copy the properties file (soht.properties) to the default
directory, and will update it with any configuration changes made using
the Administration User Interface.  The default directory is the directory
that the application server was started from.  This may not be the directory
the script was run from, as several application server start scripts change
the directory before starting the server.

To specify a properties file location other than the default directory, add
a command line parameter to the start script of the application server.  The
command line parameter should be:

-Dsoht.properties=<file location>

If you specify a location for the properties file, you must make sure that a
file exists at that location.  If not, the server will not start.  A sample
properties file is included with this distribution.

The properties are loaded and saved automatically by the application, so any
fomatting and comments in the properties file will be lost.

By default, the SOHT server writes log messages to the server
container's console.  If you wish to change the logging configuration,
you need to edit the properties file.

Usage

Once the server is deployed, it will be accessible by clients.  Please refer
to the documentation for the individual clients on their usage.

The Sever provides a web interface to modify configuration parameters.  No
changes should be made directly to the file while the server is running.
Any changes made may be lost because the administration tool overwrites this
file when configuration values are changes using the user interface.  The user
interface should be accessible at "http://&lt;yourhost&gt;:&lt;yourport&gt;/soht"
by default.

The administration web interface requires a password to access.  The default value
of this password is 'soht'.  The password should be changes immediately upon deployment.
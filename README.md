# Loiacono's FTP 959 Client
Really really REALLY simple FTP Client made with Java Sockets.  

### Usage
To run the client, you need to have Java installed on your machine.

1. Download the latest release from the [releases page](https://github.com/ReLoia/ftp-loiacono/releases/latest)
2. Execute the jar with `java -jar socket-<version>.jar`
3. Follow the instructions on the screen

#### Requirements
- Java 17 or higher

### Development Requirements
The only external library used is `commons-cli` for parsing command line arguments.  
You can find it [here](https://commons.apache.org/proper/commons-cli/).

## Project Progress
- FTP Commands supported:    
CDUP, CWD, FEAT, HELP, MKD, NOOP,  OPTS, PWD, QUIT, PASS, RMD, SITE, SIZE, STAT, SYST, USER, XPWD, XMKD, XRMD, XCWD, DELE
- FTP Commands supported with a local implementation:    
LIST, PASV, NLST, RETR, STOR, RNFR, RNTO,
- FTP Command that may be implemented in the future:  
   STOU,
- FTP Commands not supported or not implemented:  
any command from an RFC different from 959
- FTP Commands that will not be implemented:
ABOR, ACCT, ALLO, APPE, EPRT, EPSV, MODE, PORT, REIN, REST, SMNT, TYPE, STRU

### Custom Commands
- `!help` - Shows a list of available commands
- `!pwd` - Shows the current LOCAL working directory
- `!cd` - Changes the current LOCAL working directory
- `!ls` - Lists the files in the current LOCAL working directory
- `download` - Downloads a file from the server - shortcut for `PASV`, `RETR`
- `upload` - Uploads a file to the server - shortcut for `PASV`, `STOR`
- `mv` - Moves a file on the server - shortcut for `RNFR` and `RNTO`
- `!list` - Lists the files in the current server directory - shortcut for `PASV`, `LIST`
- `!nlst` - Lists the files in the current server directory without details - shortcut for `PASV`, `NLST`

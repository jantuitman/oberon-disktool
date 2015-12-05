# oberon-disktool

A Java based utility to read and write disk images for [Project Oberon](https://www.inf.ethz.ch/personal/wirth/ProjectOberon/index.html)

A image file to start with can be found here, RISCimg.zip on http://www.projectoberon.com/

The images can be run on windows/mac/linux with the [Oberon Risc Emulator](https://github.com/pdewacht/oberon-risc-emu)


##Building

This tool requires a JDK 1.6.0 compatible java compiler and java 1.6.0 runtime. you can compile the tool by running `compile.sh`


##Usage

After compiling the tool you can run `java -jar disktool.jar` to see a full list of commands. There are commands for reading/writing  files (Oberon text files or binary files), inspecting sectors, creating new image files, adding bootfiles to image files.


An example showing how you can extract files from the original RISC.img file, assuming you have downloaded it.

```

mkdir orig/
java -jar disktool.jar RISC.img extract orig/Display.rsc
java -jar disktool.jar RISC.img extract orig/Display.smb
(... repeat for every file you want to have...)

```

An example showing how you can compose a new image with a couple of files on it:  

```
#clean files
touch new.img
rm new.img

#create image
java -jar disktool.jar new.img create 1280
#copy bootfile from original image file.
java -jar disktool.jar new.img copyboot RISC.img

java -jar disktool.jar new.img add orig/Display.rsc
java -jar disktool.jar new.img add orig/Display.smb
java -jar disktool.jar new.img add orig/Fonts.smb
java -jar disktool.jar new.img add orig/Fonts.rsc
(... and so on, for every file you want to have)

```

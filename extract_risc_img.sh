#clean files
touch mijn.img
rm mijn.img
touch extract
rm -rf extract
mkdir extract

#create image
java -jar disktool.jar mijn.img create 1280
#copy boot sector
java -jar disktool.jar mijn.img copyboot ../RISC.img

#extract all files
java -jar disktool.jar ../RISC.img extract Display.rsc extract/
java -jar disktool.jar ../RISC.img extract Display.smb extract/
java -jar disktool.jar ../RISC.img extract Edit.rsc extract/
java -jar disktool.jar ../RISC.img extract Files.smb extract/
java -jar disktool.jar ../RISC.img extract Fonts.rsc extract/
java -jar disktool.jar ../RISC.img extract Fonts.smb extract/
java -jar disktool.jar ../RISC.img extract GraphicFrames.rsc extract/
java -jar disktool.jar ../RISC.img extract Graphics.rsc extract/
java -jar disktool.jar ../RISC.img extract Input.rsc extract/
java -jar disktool.jar ../RISC.img extract Input.smb extract/
java -jar disktool.jar ../RISC.img extract Kernel.smb extract/
java -jar disktool.jar ../RISC.img extract MenuViewers.rsc extract/
java -jar disktool.jar ../RISC.img extract MenuViewers.smb extract/
java -jar disktool.jar ../RISC.img extract Modules.smb extract/
java -jar disktool.jar ../RISC.img extract ORB.rsc extract/
java -jar disktool.jar ../RISC.img extract ORG.rsc extract/
java -jar disktool.jar ../RISC.img extract ORP.rsc extract/
java -jar disktool.jar ../RISC.img extract ORS.rsc extract/
java -jar disktool.jar ../RISC.img extract ORTool.rsc extract/
java -jar disktool.jar ../RISC.img extract Oberon.rsc extract/
java -jar disktool.jar ../RISC.img extract Oberon.smb extract/
java -jar disktool.jar ../RISC.img extract Oberon10.Scn.Fnt extract/
java -jar disktool.jar ../RISC.img extract Oberon10b.Scn.Fnt extract/
java -jar disktool.jar ../RISC.img extract Oberon10i.Scn.Fnt extract/
java -jar disktool.jar ../RISC.img extract Oberon12.Scn.Fnt extract/
java -jar disktool.jar ../RISC.img extract Oberon12b.Scn.Fnt extract/
java -jar disktool.jar ../RISC.img extract Oberon12i.Scn.Fnt extract/
java -jar disktool.jar ../RISC.img extract Oberon16.Scn.Fnt extract/
java -jar disktool.jar ../RISC.img extract Oberon8.Scn.Fnt extract/
java -jar disktool.jar ../RISC.img extract Oberon8i.Scn.Fnt extract/
java -jar disktool.jar ../RISC.img extract PCLink1.rsc extract/
java -jar disktool.jar ../RISC.img extract Rectangles.rsc extract/
java -jar disktool.jar ../RISC.img extract SCC.rsc extract/
java -jar disktool.jar ../RISC.img extract System.Tool extract/
java -jar disktool.jar ../RISC.img extract System.rsc extract/
java -jar disktool.jar ../RISC.img extract TextFrames.rsc extract/
java -jar disktool.jar ../RISC.img extract TextFrames.smb extract/
java -jar disktool.jar ../RISC.img extract Texts.rsc extract/
java -jar disktool.jar ../RISC.img extract Texts.smb extract/
java -jar disktool.jar ../RISC.img extract Tools.Mod extract/
java -jar disktool.jar ../RISC.img extract Viewers.rsc extract/
java -jar disktool.jar ../RISC.img extract Viewers.smb extract/

#add extracted files to new image
java -jar disktool.jar mijn.img add extract/Fonts.smb
java -jar disktool.jar mijn.img add extract/Display.rsc
java -jar disktool.jar mijn.img add extract/Display.smb
java -jar disktool.jar mijn.img add extract/Edit.rsc
java -jar disktool.jar mijn.img add extract/Files.smb
java -jar disktool.jar mijn.img add extract/Fonts.rsc
java -jar disktool.jar mijn.img add extract/GraphicFrames.rsc
java -jar disktool.jar mijn.img add extract/Graphics.rsc
java -jar disktool.jar mijn.img add extract/Input.rsc
java -jar disktool.jar mijn.img add extract/Input.smb
java -jar disktool.jar mijn.img add extract/Kernel.smb
java -jar disktool.jar mijn.img add extract/MenuViewers.rsc
java -jar disktool.jar mijn.img add extract/MenuViewers.smb
java -jar disktool.jar mijn.img add extract/Modules.smb
java -jar disktool.jar mijn.img add extract/ORB.rsc
java -jar disktool.jar mijn.img add extract/ORG.rsc
java -jar disktool.jar mijn.img add extract/ORP.rsc
java -jar disktool.jar mijn.img add extract/ORS.rsc
java -jar disktool.jar mijn.img add extract/ORTool.rsc
java -jar disktool.jar mijn.img add extract/Oberon.rsc
java -jar disktool.jar mijn.img add extract/Oberon.smb
java -jar disktool.jar mijn.img add extract/Oberon10.Scn.Fnt
java -jar disktool.jar mijn.img add extract/Oberon10b.Scn.Fnt
java -jar disktool.jar mijn.img add extract/Oberon10i.Scn.Fnt
java -jar disktool.jar mijn.img add extract/Oberon12.Scn.Fnt
java -jar disktool.jar mijn.img add extract/Oberon12b.Scn.Fnt
java -jar disktool.jar mijn.img add extract/Oberon12i.Scn.Fnt
java -jar disktool.jar mijn.img add extract/Oberon16.Scn.Fnt
java -jar disktool.jar mijn.img add extract/Oberon8.Scn.Fnt
java -jar disktool.jar mijn.img add extract/Oberon8i.Scn.Fnt
java -jar disktool.jar mijn.img add extract/PCLink1.rsc
java -jar disktool.jar mijn.img add extract/Rectangles.rsc
java -jar disktool.jar mijn.img add extract/SCC.rsc
java -jar disktool.jar mijn.img add extract/System.Tool
java -jar disktool.jar mijn.img add extract/System.rsc
java -jar disktool.jar mijn.img add extract/TextFrames.rsc
java -jar disktool.jar mijn.img add extract/TextFrames.smb
java -jar disktool.jar mijn.img add extract/Texts.rsc
java -jar disktool.jar mijn.img add extract/Texts.smb
java -jar disktool.jar mijn.img add extract/Tools.Mod
java -jar disktool.jar mijn.img add extract/Viewers.rsc
java -jar disktool.jar mijn.img add extract/Viewers.smb



java -jar disktool.jar mijn.img dir

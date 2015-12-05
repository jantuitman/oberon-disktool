package disktool;
import java.io.RandomAccessFile;
import java.io.File;

class AddFileCommand {



  void execute(ImageFile file, String name, String newName,boolean isText) {
    byte[] fileData;
     File f;
    // get the sector map.
    int sectorSize = (file.size() - file.startOffset())/1024;
    SectorMap sectorMap = new SectorMap(sectorSize);
    DirPage rootPage = new DirPage(29,file);
    rootPage.markSectorMap(sectorMap, 0 );  
    
    // read the file
    try {
        f = new File(name);
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        fileData = new byte[(int)raf.length()];
        raf.read(fileData);
    }
    catch (Exception e) {
        throw new RuntimeException(e);
    }
    if (newName == null) {
        newName = f.getName();
    }
    if (isText) {
        // change LF to CR
        for (int i=0; i<fileData.length; i++) {
            if (fileData[i] == (byte) 10) {
                fileData[i] = (byte) 13;
            }
        }
    }
    FileHeader header = FileHeader.createFromArray(file,sectorMap,newName,fileData);
    System.out.println("written file at sector: "+header.sector);
    System.out.println("aleng: "+header.aleng);
    System.out.println("bleng: "+header.bleng);

    rootPage.insert(sectorMap,header.entry);
  }
}
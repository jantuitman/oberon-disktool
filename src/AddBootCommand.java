package disktool;
import java.io.RandomAccessFile;
import java.io.File;

/*

copies a host file to the boot sector (sector 2).

*/
class AddBootCommand {
  void execute(ImageFile file,String name) {
    byte[] fileData;
    File f;
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




    int startSector = 2 * 29;
    file.writeData(
      file.sectorOffset(startSector),
      fileData);
    System.out.println("Copied "+fileData.length+" bytes, file: "+name+" to sector 2 ( to be used as boot image).");
  }  
}
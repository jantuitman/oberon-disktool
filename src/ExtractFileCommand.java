package disktool;
import java.io.RandomAccessFile;
import java.io.File;
import java.util.ArrayList;

class ExtractFileCommand {



  void execute(ImageFile file, String name,String toDir, boolean textExtract) {
    
    name = (new Utils()).convertToAscii(name);

    // retrieve the sector from the directorylisting. 
    DirPage rootPage = new DirPage(29,file);
    ArrayList<DirEntry> items = rootPage.readAll();
    DirEntry fileToFind = null;
    for (DirEntry entry : items) {
        if (entry.name.equals(name)) {
            fileToFind = entry;
            break;
        }
    }
    if (fileToFind == null) {
        throw new RuntimeException("No file named "+name+" was found.");
    }

    // read the header and the data.
    FileHeader fh = new FileHeader(fileToFind,file);
    byte[] data = fh.readFileContents();
    if (textExtract) {
        data = extractText(data);
    }

    //write the data to the host disk.
    try { 

        File output = new File(toDir);
        if (! output.isDirectory()) {
            throw new RuntimeException("Specify a directory where to write the file");
        }

        RandomAccessFile output2 = new RandomAccessFile(new File(output,name),"rw");
        output2.setLength(data.length);
        output2.write(data);
    }
    catch (Exception e) {
        throw new RuntimeException(e);
    }
  }



  byte[] extractText(byte[] data) {
    if (data[0] != (byte) 0xF1) {
      throw new RuntimeException("The file read is no Oberon text file");
    } 
    int textOffset = unsigned(data[1]) + unsigned(data[2]) * 255 + unsigned(data[3]) * (255 * 255) + unsigned(data[4]) * (255 * 255 * 255);
    int textLength = unsigned(data[textOffset - 4]) + unsigned(data[textOffset - 3]) * 255 + unsigned(data[textOffset - 2]) * (255 * 255) + unsigned(data[textOffset - 1]) * (255 * 255 * 255);
    if (textOffset + textLength != data.length) {
      System.out.println("inconsistent length: header length "+textOffset+ "+ text length " + textLength +" !=  total file length" +data.length);
    }else {
      System.out.println("text header length "+textOffset+ "+ text length " + textLength +" ==  total file length" +data.length);

    }
    byte[] result = new byte[data.length - textOffset ];
    System.arraycopy(data,textOffset,result,0,data.length - textOffset );

    // fix CR/LF
    for (int i = 0; i < result.length ; i++) {
      if (result[i] == (byte) 13) {
        result[i] = (byte) 10;
      }
    }
    return result;
  }

  int unsigned(byte b) {
    int result = b & 0xFF;
    return result;
  }


}


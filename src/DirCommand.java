package disktool;

import java.util.ArrayList;

class DirCommand {

  DirCommand() {

  }
  
// int val = -32768;
// String hex = Integer.toHexString(val);

// int parsedResult = (int) Long.parseLong(hex, 16);
// System.out.println(parsedResult);

  void execute(ImageFile file) {
    //int sectorOffset = file.sectorOffset(29);
    
    DirPage rootPage = new DirPage(29,file);
    ArrayList<DirEntry> items = rootPage.readAll();
    for(DirEntry item : items) {
      System.out.println(item.name);
    }


    //int data = file.readInt(sectorOffset);
    
    /*
    // 0x9B1EA38D
    if (data == 0x9b1ea38d ) {
      System.out.println("mark was found");
    }
    else {
      throw new RuntimeException("unexpected mark at sector 29 (page 1)");

    }
    System.out.println("mark = "+Integer.toHexString(data));
    System.out.println("m (number of elements) = "+Integer.toHexString(file.readInt(sectorOffset + 4)));
    System.out.println("p0 = "+ file.readInt(sectorOffset + 8));
    int m = file.readInt(sectorOffset + 4);
    for(int i=0; i < m ; i++) {
      System.out.println("Filename[" + i + "] = >" + file.readString(sectorOffset + 64 + i * 40,32)+"<");
      System.out.println("Sector[" + i + "] = " + file.readInt(sectorOffset + 64 + i * 40 + 32) );
      System.out.println("node-p[" + i + "] = " + file.readInt(sectorOffset + 64 + i * 40 + 36) );

    }
    */
    file.close();
  }
}
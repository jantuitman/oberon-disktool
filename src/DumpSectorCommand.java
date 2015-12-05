package disktool;

class DumpSectorCommand {

  void execute(ImageFile file,int startSector) {
    int length =  1024;
    byte[] data = file.readData(file.sectorOffset(startSector),length);

    for (int k = 0; k < data.length ; k = k + 16) {
      String line = "";
      String asciiline = "";
      line = line + String.format("%04x ", k);
      for (int j=0; j < 16 ; j ++ ) {
        if (k + j >= data.length) break;

        line = line + String.format("%02x ",data[k+j]);
        if (data[k+j] >= 32 && data[k+j] < 127) {
          asciiline = asciiline + (char) data[k+j];
        } 
        else {
          asciiline = asciiline + '.';
        }
      }
      line = line + asciiline;
      System.out.println(line);
    }
  }
}
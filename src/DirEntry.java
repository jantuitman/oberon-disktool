package disktool;

class DirEntry {
  
  String name ;
  int sector;
  
  /** takes 36 bytes : 32 name, 4 sector. */
  static DirEntry read(ImageFile file, int pos ) {
    DirEntry result = new DirEntry();
    result.name = file.readString(pos,32);
    result.sector = file.readInt(pos + 32);
    return result;
  }

  void write(ImageFile file, int pos) {
    System.out.println("Write Dir entry: "+name);

    file.putString(pos, 32, name);
    if (sector % 29 != 0) {
      throw new RuntimeException("sector should be a sector number");
    }
    file.putInt(pos + 32,sector);
  } 
}
package disktool;
import java.util.ArrayList;

class FileHeader {
  
  DirEntry entry;
  ImageFile file;
  int sector ;
  int offset ;
  int aleng; // number of completely filled sectors, including this one, but excluding the extended sectors. 
  int bleng; // number of bytes in the last sector.
  int date ;
  static final int HEADER_SIZE = 352;
  static final int SECTOR_SIZE = 1024;
  
  FileHeader() {}

  FileHeader(DirEntry dirEntry,ImageFile imageFile) {
    entry = dirEntry;
    file = imageFile;
    sector = entry.sector;
    offset = file.sectorOffset(sector);

    if (file.readInt(offset + 0) != 0x9BA71D86 ) {
      throw new RuntimeException("trying to open a file at sector "+sector+" but mark not found there.");
    }
    aleng = file.readInt( offset + 36);
    bleng = file.readInt( offset + 40);
    date = file.readInt( offset + 44);
  }

  static FileHeader createFromArray(ImageFile imageFile,SectorMap map, String name,byte[] input) {
    if (name.length() > 32 ) throw new RuntimeException("Filename must be max 32 characters");
    int[] sectors = map.allocateSectors(input.length + HEADER_SIZE,"new file:"+name);
    FileHeader header = new FileHeader();
    header.file = imageFile;
    header.sector = sectors[0];
    header.offset = header.file.sectorOffset(sectors[0]);
    header.aleng = (input.length + HEADER_SIZE) / SECTOR_SIZE ;
    header.bleng = (input.length + HEADER_SIZE) % SECTOR_SIZE ;
    if (header.aleng >= 64) throw new RuntimeException("Big files not supported yet");
    header.entry = new DirEntry();
    header.entry.name = name;
    header.entry.sector = header.sector;
    header.putHeaderData();
    for (int a = 0; a < sectors.length; a++) {
      imageFile.putInt(header.offset + 96 + a * 4,sectors[a]);
    }
    // now fill the data.
    for (int i=0; i< input.length; i++) {
      int offset = HEADER_SIZE + i;
      int sec = sectors[offset / SECTOR_SIZE];
      int secOffset = offset % SECTOR_SIZE;
      imageFile.putByte(imageFile.sectorOffset(sec) + secOffset, input[i]);
    }
    return header;
  }

  // writes all data and sets the sector tables to zero.
  void putHeaderData() {
    offset = file.sectorOffset(sector);
    file.putInt(offset + 0   , 0x9BA71D86);
    file.putString(offset + 4, 32, entry.name);
    file.putInt(offset + 36   , aleng); System.out.println("aleng = "+aleng);
    file.putInt(offset + 40  , bleng);
    file.putInt(offset + 44  , 406979665); // DATE : toDo.
    // erase the sector tables.
    for (int i=0 ; i< 12 + 64; i++) {
      file.putInt(offset + 48 + i * 4, 0);
    }
  }

  byte[] readFileContents() {
    int fileLength = aleng * SECTOR_SIZE + bleng - HEADER_SIZE;
    byte[] result = new byte[fileLength];
    int resCount = fileLength;
    int pos = 0;
    

    int dataFromFirstSector = Math.min(resCount, SECTOR_SIZE - HEADER_SIZE);
    byte[] data = file.readData(offset + HEADER_SIZE,dataFromFirstSector);
    System.arraycopy(data,0,result,pos,dataFromFirstSector);
    resCount = resCount - dataFromFirstSector;
    pos = pos + dataFromFirstSector;
    

    // calculate the amount of sectors refwerenced from the header sector and from extendedsectors.
    int numberOfExtendedSectors = 0;
    int numberOfNormalSectors = 0;
    // 1 header size, 64 simple sectors.
    if (aleng -  64 > 0 ) {
      throw new RuntimeException("reading big files not supported yet");
    }
    else {
        // we dont count the header in as a normalSector so -1.
        numberOfNormalSectors = aleng ;
        // add one for the rest bytes.
        if (bleng > 0) {
          if (numberOfNormalSectors == 64) {
            throw new RuntimeException("reading big files not supported yet");
          }
          else {
            numberOfNormalSectors++;
          }
        }
    }

    for (int a= 1; a < numberOfNormalSectors && resCount > 0; a++) {
      int dataFromSector = Math.min(resCount,SECTOR_SIZE);
      int sectorNumber =  file.readInt(offset + 96 + a * 4);
      data = file.readData(file.sectorOffset(sectorNumber),dataFromSector);
      System.arraycopy(data,0,result,pos,dataFromSector);
      resCount = resCount - dataFromSector;
      pos = pos + dataFromSector;
    }
    System.out.println("File lenght :"+fileLength+" bytes");
    System.out.println("Read :"+ pos +" bytes");
    return result;
  }

  void markSectorMap(SectorMap map) {

    // calculate the amount of sectors refwerenced from the header sector and from extendedsectors.
    int numberOfExtendedSectors = 0;
    int numberOfNormalSectors = 0;
    // 1 header size, 64 simple sectors.
    if (aleng -  64 > 0 ) {
      numberOfNormalSectors = 64;
      numberOfExtendedSectors = aleng -  64;
      // add one for the rest bytes.
      if (bleng > 0) {
        numberOfExtendedSectors++;
      }
    }
    else {
        // we dont count the header in as a normalSector so -1.
        numberOfNormalSectors = aleng ;
        // add one for the rest bytes.
        if (bleng > 0) {
          if (numberOfNormalSectors == 64) {
            numberOfExtendedSectors = 1;
          }
          else {
            numberOfNormalSectors++;
          }
        }
    }
    
    map.markSector(sector,"fileheader : file "+entry.name+"  / numberOfNormalSectors: "+numberOfNormalSectors+" extendedSectors "+numberOfExtendedSectors);
    for (int a=0; a < numberOfNormalSectors ; a++) {
      int sectorAddress =file.readInt(offset + 96 + a * 4);
      if (sectorAddress == 0) {
        throw new RuntimeException("Expected more sectors");
      }
      if (a == 0) {
        // should already be marked.
        if (!map.isMarked(sectorAddress)) {
          throw new RuntimeException("We expect this to be already marked.");
        }       
      }
      else { 
        map.markSector(sectorAddress,"datasector: file "+entry.name+ " / sector "+ a +" after header");
      }
    }
    if (numberOfExtendedSectors / 256 >= 12) {
      throw new RuntimeException("Found a really big file, not good implemented.");
    } 

    for (int b = 0; b < numberOfExtendedSectors ; b++) {
      // fetch and mark the sector table.  
      int sectorAddress =file.readInt(offset + 48 + (b/256) * 4);
      if (sectorAddress == 0) {
        throw new RuntimeException("Expected more extended sectors");
      } 
      if (b % 256 == 0) {
        map.markSector(sectorAddress,"file sector table, file: "+entry.name);
      }
      int extendedSectorAddress = file.readInt(file.sectorOffset(sectorAddress) + (b % 256) * 4);
      map.markSector(extendedSectorAddress,"datasector (linked via extended), file : "+entry.name+ " / sector "+(b+64)+" after header");
    }
  }


}
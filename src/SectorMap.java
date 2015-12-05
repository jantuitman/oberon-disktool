package disktool;

class SectorMap {

  String[] sectors ;

  SectorMap(int size) {
    sectors = new String[size];
    sectors[0] = "- not a valid sector address, because no multiple of 29-";
    for (int i=2; i< 64 ; i++) {
      sectors[i] = "- Marked as used by Kernel.Mod -";
    }
  }

  int[] allocateSectors(int byteSize,String used_by) {
    int sectorsRequired = (byteSize / 1024);
    if (byteSize % 1024 > 0) {
      sectorsRequired++;
    }
    int[] result = new int[sectorsRequired];
    for (int i = 0; i < result.length ; i++ ) {
      // brute force search
      result[i] = allocateSector(used_by);
      if (result[i] == 0) throw new RuntimeException("Out of disk space, sorry!");
    } 
    return result;
  }  
  
  int allocateSector(String used_by) {
    // brute force search
    for (int j = 0; j < sectors.length; j++ ) {
      if (sectors[j] == null) {
        sectors[j] = used_by;
        return j * 29;
      }
    }
    throw new Error("Out of disk space, sorry!");
  }

  void markSector(int sectorRef,String used_by){
    markSector(sectorRef,used_by,false);
  }


  void markSector(int sectorRef,String used_by,boolean force){
    if (sectorRef % 29  != 0) {
      throw new RuntimeException("The sector value you passed in is not denoting a sector. it should be a multiple of 29 and not " + sectorRef);
    }
    if (sectors[sectorRef/29] != null && !force) {
      throw new RuntimeException("sector is already marked. new:"+used_by+" existing: "+sectors[sectorRef/29]);
    }
    sectors[sectorRef / 29] = used_by;
  }

  boolean isMarked(int sectorRef) {
    if (sectorRef % 29  != 0) {
      throw new RuntimeException("The sector value you passed in is not denoting a sector. it should be a multiple of 29 and not " + sectorRef);
    }
    return (sectors[sectorRef / 29] != null);    
  }

  void freeSector(int sectorRef) {
    if (sectorRef % 29  != 0) {
      throw new RuntimeException("The sector value you passed in is not denoting a sector. it should be a multiple of 29 and not " + sectorRef);
    }
    sectors[sectorRef / 29] = null;
  }
  
  void listAll() {
    System.out.println("there are "+sectors.length+" sectors");
    int free = 0;
    int used = 0;
    for (int i=0; i < sectors.length ; i++) {
      String s ;
      if (sectors[i] == null) {
        s = "FREE SECTOR";
        free++;
      }
      else {
        s = sectors[i];
        used++;
      }
      System.out.println("" + i +" (*29) :  "+ s);
    }
    System.out.println("free: "+free + " used: "+used);
  }
}
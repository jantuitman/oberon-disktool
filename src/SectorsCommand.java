package disktool;


class SectorsCommand {


  void execute(ImageFile file) {
    int sectorSize = (file.size() - file.startOffset())/1024;
    SectorMap sectorMap = new SectorMap(sectorSize);
    DirPage rootPage = new DirPage(29,file);
    rootPage.markSectorMap(sectorMap, 0 );  
    sectorMap.listAll();
  }

}
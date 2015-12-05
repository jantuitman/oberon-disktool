package disktool;

/*

copies the bootsectors of the second  image file to the first.

*/
class CopyBootCommand {
  void execute(ImageFile file,ImageFile sourceFile) {
    int startSector = 2 * 29;
    int length = (64 - 2) * 1024;
    file.writeData(
      file.sectorOffset(startSector),
      sourceFile.readData(sourceFile.sectorOffset(startSector),
      length
    ));
    System.out.println("Copied boot sectors.");
  }  
}
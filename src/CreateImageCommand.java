package disktool;


class CreateImageCommand {
  void execute(String name,int size,boolean large) {
    if (size < 65) {
      throw new RuntimeException("the Oberon system reserves the first 64K of the diskimage for a bootable ram image. So a disk < 65 sectors cannot have any files. Please specify at least 65 as size.");
    }
    ImageFile file = new ImageFile(name,size,large);
    DirPage page = DirPage.createNew(file,29); // create a dir at sector 29.
    System.out.println("Empty root dir created on new imagefile.");
  }  
}
package disktool;

class GrowCommand {

  void execute(ImageFile file, int sectors) {
    file.grow(sectors);
    System.out.println("filesize was increased.");
  }
}
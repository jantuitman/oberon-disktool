package disktool;

class Main {

  static void usage() {
    System.out.println("Usage: java -jar disktool.jar <imagefile>  <command> ... extra arguments");
    System.out.println("\nWhere command is:\n");
    
    System.out.println("\nReading/Writing files:\n\n");
    System.out.println("add <file>                 - add a new file, copying it from the given host file <file>. the file will have the same name as the source.");
    System.out.println("add <file> <name>          - add a new file, copying it from the given host file <file>. The file will have a new name.");
    System.out.println("addtext <file>             - see add, but also performs conversion of LF to CR. CR ascii files open correctly in the oberon edit program.");
    System.out.println("addtext <file> <name>      - see add, but also performs conversion of LF to CR. CR ascii files open correctly in the oberon edit program.");
    System.out.println("extract <file> <todir>     - copies a file from the image to the host file system.");
    System.out.println("extracttext <file> <todir> - copies a file from the image to the host file system, converting oberon text files to ascii files.");
    
    System.out.println("\nDirectory/ sector inspection:\n\n");
    System.out.println("dir                        - show directory");
    System.out.println("listsectors                - dump the sector map.");
    System.out.println("dump <sectorno>            - shows a hexdump of a single sector.");

    System.out.println("\nManaging image files:\n\n");
    System.out.println("create <n>                 - creates or overwrites the image file specified with an empty image file containing n sectors of 1024 bytes.");
    System.out.println("create <n> large           - Same as above, but prefixes the new image file with  0x10000000 + 1024 zeroes, to be compatible with the real oberon compactflash images.");
    System.out.println("grow <n>                   - increase the file with n sectors (1024 zero bytes).");

    System.out.println("\nAdding the bootfile:\n\n");
    System.out.println("copyboot <filename>        - copies the boot sectors (sector 2 * 29 - 63 * 29) of the specified file (an image file) to the current file.");
    System.out.println("addboot <filename>         -  writes a file from the host to the boot sectors.");
    
  }


  public static void main(String[] args) {
    if (args.length < 2) {
      usage();
      return;
    }
    if (args[1].equals("dir")) {
      DirCommand command = new DirCommand();
      ImageFile file = new ImageFile(args[0]);
      command.execute(file);
      return;
    }
    if (args[1].equals("listsectors")) {
      SectorsCommand command = new SectorsCommand();
      ImageFile file = new ImageFile(args[0]);
      command.execute(file);
      return;
    }
    if (args[1].equals("grow")) {
      GrowCommand command = new GrowCommand();
      ImageFile file = new ImageFile(args[0]);
      int sectors = Integer.parseInt(args[2]);
      command.execute(file,sectors);
      return;
    }
    if (args[1].equals("add")) {
      AddFileCommand command = new AddFileCommand();
      ImageFile file = new ImageFile(args[0]);
      if (args.length >= 4) {
        command.execute(file,args[2],args[3],false);
      }
      else {
        command.execute(file,args[2],null,false);
      }
      return;
    }
    if (args[1].equals("addtext")) {
      AddFileCommand command = new AddFileCommand();
      ImageFile file = new ImageFile(args[0]);
      if (args.length >= 4) {
        command.execute(file,args[2],args[3],true);
      }
      else {
        command.execute(file,args[2],null,true);
      }
      return;
    }
    if (args[1].equals("create")) {
      CreateImageCommand command = new CreateImageCommand();
      int sectors = Integer.parseInt(args[2]);
      if (args.length >= 4 && args[3].equals("large")) {
        command.execute(args[0],sectors,true);
      }
      else {
        command.execute(args[0],sectors,false);
      }
      return ;
    }
    if (args[1].equals("addboot")) {
      AddBootCommand command = new AddBootCommand();
      ImageFile file = new ImageFile(args[0]);
      command.execute(file,args[2]);
      return;
    }
    if (args[1].equals("copyboot")) {
      CopyBootCommand command = new CopyBootCommand();
      ImageFile file = new ImageFile(args[0]);
      ImageFile file2 = new ImageFile(args[2]);
      command.execute(file,file2);
      return ;
    }
    if (args[1].equals("dump")) {
      DumpSectorCommand command = new DumpSectorCommand();
      ImageFile file = new ImageFile(args[0]);
      int sector = Integer.parseInt(args[2]);
      command.execute(file,sector * 29);
      return ;
    }
    if (args[1].equals("extract")) {
      ExtractFileCommand command = new ExtractFileCommand();
      ImageFile file = new ImageFile(args[0]);

      command.execute(file,args[2],args[3],false);
      return ;
    }
    if (args[1].equals("extracttext")) {
      ExtractFileCommand command = new ExtractFileCommand();
      ImageFile file = new ImageFile(args[0]);

      command.execute(file,args[2],args[3],true);
      return ;
    }


    usage();
  }
}
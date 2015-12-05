package disktool;

import java.util.ArrayList;

class DirPage {
  int sector;
  int offset;
  ImageFile file;
  DirPage preEntry ;
  ArrayList<DirEntry> entries = new ArrayList<DirEntry>(); // length should be equal to postEntries for non leaf nodes
  ArrayList<DirPage> postEntries  = new ArrayList<DirPage>();

  final static int MAX_CAPACITY = 24;
  
  static DirPage createNew(ImageFile file,int sector) {
    DirPage result = new DirPage() ;
    result.sector = sector; 
    result.offset = file.sectorOffset(sector);
    result.file = file;
    result.save();
    return result;
  }

  private DirPage() {}

  DirPage(int sector,ImageFile imgf) {
     file = imgf;
     this.sector = sector;
     offset  = file.sectorOffset(sector);

     if (file.readInt(offset + 0) != 0x9b1ea38d) {
        throw new RuntimeException("not a dirpage af offset "+offset) ;
     }
     int entryCount = file.readInt(offset + 4);
     if (file.readInt(offset + 8) != 0) {
        preEntry = new DirPage(file.readInt(offset + 8),file) ;
     }

     for (int i=0;i < entryCount ; i++) {
        DirEntry dd = DirEntry.read(file, offset + 64 + i * 40);
        entries.add(dd);
        //System.out.println("read entry: "+dd.name);
        int postEntrySector = file.readInt(offset + 64 + i * 40 + 36);
        if ( preEntry != null && postEntrySector != 0) {
          postEntries.add(new DirPage(postEntrySector,file));  
        }
         
     }
  }

  void save() {
    file.putInt(offset + 0 , 0x9b1ea38d);
    file.putInt(offset + 4, entries.size());
    if (preEntry == null) {
      file.putInt(offset + 8, 0);
    }
    else {
      file.putInt(offset + 8, preEntry.sector); 
      if (preEntry.sector % 29 != 0 || preEntry.sector == 0) {
        throw new RuntimeException("sector should be a sector number");
      }
    }
    for(int i = 12; i< 64; i ++ ) {
      file.putByte(offset + i, (byte) 0);
    }
    for(int i = 0; i < entries.size() ; i ++) {
      DirEntry d = entries.get(i);
      d.write(file,offset + 64 + i * 40); // writes 36 bytes (name,sector)
      if ( i < postEntries.size()) {
        DirPage postEntry = postEntries.get(i); 
        file.putInt(offset + 64 + i * 40 + 36, postEntry.sector);
        if (postEntry.sector % 29 != 0 || postEntry.sector == 0 ) {
          throw new RuntimeException("sector should be a sector number");
        }
      }
      else {
        file.putInt(offset + 64 + i * 40 + 36, 0);
      }
    }
    System.out.println("Saving DirPage... Done.");
  }

  ArrayList<DirEntry> readAll() {
    ArrayList<DirEntry> result = new ArrayList<DirEntry>();
    if (preEntry != null) {
      result.addAll(preEntry.readAll());
    }
    for (int i=0;i<entries.size() ; i ++) {
      result.add(entries.get(i));
      if (postEntries.size() > i ) {
        result.addAll(postEntries.get(i).readAll());
      }
    } 
    return result;
  }
  
/* ------------------------------ inserting direntries ------------------*/
  static class InsertionResult {
    boolean isOverflow ;
    DirEntry splitEntry;
    DirPage splitPage;

    static InsertionResult noOverflow() {
      InsertionResult result = new InsertionResult();
      result.isOverflow = false ;
      return result;
    }
  }


  // the rootnode is a special case which is handled here.
  // btrees are built bottom up, but we are required to keep the
  // root dirpage on the same address. therefore the root has its own 
  // overflow handling technique.
  void insert(SectorMap map, DirEntry newEntry) {
    InsertionResult result = insertEntry(map,newEntry);
    if (result.isOverflow) {
      // assume: a new page is already created in result.splitPage.
      // assume: the current page only contains the first half of the items, minus the splitEntry.

      int sectorAddress = map.allocateSector("new dirpage");
      DirPage replacementForSelf = DirPage.createNew(file,sectorAddress);
      replacementForSelf.entries = this.entries;
      replacementForSelf.preEntry = this.preEntry;
      replacementForSelf.postEntries = this.postEntries; 
      replacementForSelf.save();
      
      // now erase this.
      this.preEntry = new DirPage(sectorAddress,file); // this is a reload of the just saved replacementForSelf.
      this.entries.clear();
      this.postEntries.clear();
      // add the one item from the overflow  
      this.entries.add(result.splitEntry);
      this.postEntries.add(result.splitPage);

      this.save(); // write to disk.

      // erase this page to have only the overflow entry.
    }
  }

  InsertionResult insertEntry(SectorMap map, DirEntry newEntry) {
    int cmp = 0;
    // handle leaf searching in another function.
    if (preEntry == null) {
      return insertEntryInLeaf(map,newEntry);
    }
    // else we are in non leaf.
    cmp = newEntry.name.compareTo(entries.get(0).name);
    if ( cmp < 0 ) {
      InsertionResult result = preEntry.insertEntry(map,newEntry);
      return handleInsertionResult(map,result);
     }
    if (cmp == 0) {
      throw new RuntimeException("File Already exists");
    }

    // repeat the above for all items after entries.get(0)

    for( int i=0; i < postEntries.size() ; i++) {
      if (i + 1 < entries.size()) {
        cmp = newEntry.name.compareTo(entries.get(i + 1).name) ;
        if (cmp < 0) {
          InsertionResult result = postEntries.get(i).insertEntry(map,newEntry);
          return handleInsertionResult(map,result);          
        }
        if (cmp == 0) {
          throw new RuntimeException("File Already exists");
        }

      }
      else if ( i + 1 == entries.size()) {
        // we are after the last item of the entries list and our name is still not matched.
        // but we still have one postEntries childlist.
        // so we must insert it in that.
        InsertionResult result = postEntries.get(i).insertEntry(map,newEntry);
        return handleInsertionResult(map,result);          
      }
      else {
        //apperently there is an entry, but no corresponding postentries 
        throw new RuntimeException("Internal inconsistency error");
      }
    }  

    throw new RuntimeException("Should not be able to reach this line.");
  }
  
  InsertionResult insertEntryInLeaf(SectorMap map, DirEntry entry) {
    int insertIndex = -1 ;
    for( int i=0; i< entries.size() ; i++) {
      int cmp = entry.name.compareTo(entries.get(i).name);
      if (cmp == 0) {
        throw new RuntimeException("File already exists");
      }
      else if (cmp < 0) {
        insertIndex = i;
        break;
      }
    }   

    if (insertIndex == -1) {
      // found no insertion point, append at end.
      entries.add(entry);
    }
    else {
      entries.add(insertIndex,entry);
    }

    // check for overflow.
    if (entries.size() > MAX_CAPACITY) {
      System.out.println("----------------------- LEAF INSERTION OVERFLOW HANDLING -------------------");

      InsertionResult result = new InsertionResult();
      result.isOverflow = true;
      result.splitEntry = entries.get(MAX_CAPACITY / 2);
      ArrayList<DirEntry> moveEntries = 
        new ArrayList<DirEntry>( entries.subList(MAX_CAPACITY / 2 + 1, MAX_CAPACITY + 1));
      // create a new page from moveentries;
      int sectorAddress = map.allocateSector("new dirpage");
      System.out.println("allocated sector was : "+sectorAddress);
      result.splitPage = DirPage.createNew(file,sectorAddress);
      result.splitPage.entries = moveEntries;
      result.splitPage.preEntry = null ; // leaf node
      result.splitPage.save();
      // now save a trimmed version of myself.
      entries = new ArrayList<DirEntry>(entries.subList(0 , MAX_CAPACITY / 2)); 
      this.save();
      return result;
    }
    else {
      this.save();
      return InsertionResult.noOverflow();
    }
  }


  InsertionResult handleInsertionResult(SectorMap map,InsertionResult childResult) {
    if (childResult.isOverflow ){
      System.out.println("----------------------- CHILD INSERTION OVERFLOW HANDLING -------------------");
      int insertIndex = -1 ;
      for( int i=0; i< entries.size() ; i++) {
        int cmp = childResult.splitEntry.name.compareTo(entries.get(i).name);
        if (cmp == 0) {
          throw new RuntimeException("File already exists");
        }
        else if (cmp < 0) {
          insertIndex = i;
          break;
        }
      }   

      if (insertIndex == -1) {
        // found no insertion point, append at end.
        entries.add(childResult.splitEntry);
        postEntries.add(childResult.splitPage);
      }
      else {
        entries.add(insertIndex,childResult.splitEntry);
        postEntries.add(insertIndex,childResult.splitPage);
      }

      // check if we have overflow again in the parent page.
      if (entries.size() > MAX_CAPACITY) {
        //trim our items. create the new page.
        //create the insertion result. 
        InsertionResult result = new InsertionResult();
        result.isOverflow = true;
        result.splitEntry = entries.get(MAX_CAPACITY / 2);
        ArrayList<DirEntry> moveEntries = 
          new ArrayList<DirEntry>( entries.subList(MAX_CAPACITY / 2 + 1, MAX_CAPACITY + 1));
        ArrayList<DirPage> movePostEntries = 
          new ArrayList<DirPage>( postEntries.subList(MAX_CAPACITY / 2 + 1, MAX_CAPACITY + 1));      
        DirPage newPreEntryOfPostList =   postEntries.get(MAX_CAPACITY / 2);
        // create a new page from moveentries;
        int sectorAddress = map.allocateSector("new dirpage");
        result.splitPage = DirPage.createNew(file,sectorAddress);
        result.splitPage.entries = moveEntries;
        result.splitPage.preEntry = newPreEntryOfPostList;
        result.splitPage.postEntries = movePostEntries;
        result.splitPage.save();
        // now save a trimmed version of myself.
        entries = new ArrayList<DirEntry>(entries.subList(0 , MAX_CAPACITY / 2)); 
        postEntries = new ArrayList<DirPage>(postEntries.subList(0 , MAX_CAPACITY / 2)); 
        this.save();
        return result;


      }
      else {
        this.save();
        return InsertionResult.noOverflow();
      }

    }
    else {
      return InsertionResult.noOverflow();
    }
  }


/*
  DirEntry insert(DirEntry newEntry) {
    int cmp = 0;

    if (preEntry == null) {
      DirEntry pushedItem = insertInLeafNode(newEntry);
      if (pushedItem == null) return null;
      throw new RuntimeException("Insert in non leaf node not yet supported.");
    }

    cmp = newEntry.name.compareTo(entries.get(0).name);
    if ( cmp < 0 ) {
      DirEntry pushedItem = preEntry.insert(newEntry);
      if (pushedItem == null) return null;
      throw new RuntimeException("Insert in non leaf node not yet supported.");
    }
    if (cmp == 0) {
      throw new RuntimeException("File Already exists");
    }

    for( int i=0; i < postEntries.size() ; i++) {
      if (i + 1 < entries.size()) {
        cmp = newEntry.name.compareTo(entries.get(i + 1).name) ;
        if (cmp < 0) {
          DirEntry pushedItem = postEntries.get(i).insert(newEntry);
          if (pushedItem == null) return null;
          throw new RuntimeException("Insert in non leaf node not yet supported.");
        }
        if (cmp == 0) {
          throw new RuntimeException("File Already exists");
        }
      }
      else if ( i + 1 == postEntries.size()) {
        DirEntry pushedItem = postEntries.get(i).insert(newEntry);
        if (pushedItem == null) return null;
        throw new RuntimeException("Insert in non leaf node not yet supported.");
      }
      else {
        throw new RuntimeException("tree invariant not valid.");
      }
    }

    // WHAT TO DO HERE>
    return null;
  }

  DirEntry insertInLeafNode(DirEntry entry) {
    for( int i=0; i< entries.size() ; i++) {
      int cmp = entry.name.compareTo(entries.get(i).name);
      if (cmp == 0) {
        throw new RuntimeException("File already exists");
      }
      else if (cmp < 0) {
        // found an insert point.
        if (entries.size() == MAX_CAPACITY) {
          // break the thing in two.
          throw new RuntimeException("B-tree overflowing not yet supported");
          //return
        }
        // simple insert.
        entries.add(i,entry);
        this.save();
        return null;  
      }
      // if cmp >  0 we need to iterate further.
    }
    if (entries.size() == MAX_CAPACITY) {
      // break the thing in two.
      throw new RuntimeException("B-tree overflowing not yet supported");
      //return
    }
    // simple insert.
    entries.add(entry);
    this.save();
    return null; 
  }
*/

  void markSectorMap(SectorMap map, int level) {
    map.markSector(this.sector,"DirPage - level "+level);
    if (preEntry != null) {
      preEntry.markSectorMap(map,level + 1);
    }
    for (int i=0;i<entries.size() ; i ++) {
      FileHeader fileHeader = new FileHeader(entries.get(i),file);
      fileHeader.markSectorMap(map);
      if (i < postEntries.size()  ) {
        postEntries.get(i).markSectorMap(map, level + 1);
      }
    } 
    
  }
} 
package disktool;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.MappedByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

class ImageFile {

  RandomAccessFile raf;
  MappedByteBuffer mbb ; 
  FileChannel channel ;
  int size;
  int startOffset ; //0x10000000 or -1024, depending if the magic mark was found on the start of the file.
  
  ImageFile(String name) {
    try {
      File file = new File(name);
      raf = new RandomAccessFile(file,"rw");
      channel = raf.getChannel();
      size = (int) channel.size();
      mbb = channel.map(FileChannel.MapMode.READ_WRITE, 0, size );
      mbb.order(java.nio.ByteOrder.LITTLE_ENDIAN);
      // check if sector(1) is at location 0 or not.
      // if its not, assume  0x10000000 + 1024.
      // sector one is the root directory, so it can be recognized with the 
      // directory marker.
      if (mbb.getInt(0) == 0x9b1ea38d) {
        startOffset = -1024; // the minus 1024 is needed to skip the nonexisting sector 0.
      }
      else {
        startOffset = 0x10000000 ; 
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // create a new file.
  ImageFile(String name, int sectorSize,boolean large) {
    try {
      if (large) {
        startOffset = 0x10000000 ;
      }
      else {
        startOffset = -1024;
      }
      //startOffset = 0;
      File file = new File(name);
      size = sectorSize * 1024;
      int bufferSize = 0;
      if (startOffset < 0) {
        bufferSize = size;
      }
      else {
        bufferSize = size + startOffset;
      }


      raf = new RandomAccessFile(file,"rw");
      raf.setLength(bufferSize );
      channel = raf.getChannel();
      mbb = channel.map(FileChannel.MapMode.READ_WRITE, 0, bufferSize);
      mbb.order(java.nio.ByteOrder.LITTLE_ENDIAN);
      mbb.position(0);
      byte[] dest = new byte[bufferSize ];
      mbb.put(dest,0,bufferSize );
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  int startOffset() {
    return startOffset;
  }

  int size() {
    return size;
  }



  /** grows the file with a number of sectors */
  void grow(int sectors) {
    int newLength = size + 1024 * sectors;
    try {

      // adjust the size
      raf.setLength(newLength);

      // recreate the memory mapped buffer
      channel = raf.getChannel();
      int oldSize = size;
      size = (int) channel.size();
      mbb = channel.map(FileChannel.MapMode.READ_WRITE, 0, size);
      for (int i = oldSize + 1; i < size ; i++) {
        mbb.put(i,(byte) 0);
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  } 

  // int sectorRef(int logicalSector) {
  //   return logicalSector * 29;
  // }


  // int sectorOffset(int sectorRef) {
  //   return 0x10000000 + (sectorRef / 29) * 1024;
  // }
  int sectorOffset(int sectorRef) {
    if (sectorRef % 29  != 0) {
      throw new RuntimeException("The sector value you passed in is not denoting a sector. it should be a multiple of 29 and not " + sectorRef);
    }
    return (sectorRef/29) * 1024 + this.startOffset();
  }

  int readInt(int offset) {
    return mbb.getInt(offset); // long at byte 40,000,000
  }
  void putInt(int offset, int value) {
    mbb.putInt(offset,value);
  }

  byte readByte(int offset) {
    return mbb.get(offset);
  }
  void putByte(int offset,byte value) {
    mbb.put(offset,value);
  }

  void writeData(int offset,byte[] data) {
    mbb.position(offset);
    mbb.put(data,0,data.length);
  }
  byte[] readData(int offset,int length) {
    byte[] result = new byte[length];
    mbb.position(offset);
    mbb.get(result,0,length);
    return result;
  }

  String readString(int offset,int length) {
    byte[] dest = new byte[length]; 
    mbb.position(offset);
    mbb.get(dest,0,length);
    int myLength = length;
    for (int i=0;i< dest.length ; i++) {
      if (dest[i] == (byte) 0) {
        myLength = i;
        break;
      }
    }
    byte[] dest2 = new byte[myLength];
    System.arraycopy(dest,0,dest2,0,myLength);
    try {
      return new String(dest2, "US-ASCII");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  void putString(int offset,int length, String value) {
    byte[] dest = new byte[length];
    for (int i=0; i < dest.length ; i++) dest[i] = 0;
    byte[] str = value.getBytes(Charset.forName("US-ASCII"));
    System.arraycopy(str ,0 ,dest ,0 , Math.min(dest.length,str.length) ); 
    mbb.position(offset);
    mbb.put(dest,0,length);
  }

  void close() {
    try {
      mbb.force();
      channel.close();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
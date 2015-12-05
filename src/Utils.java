package disktool;

class Utils {


  String convertToAscii(String input) {
    try {
      return new String(input.getBytes("US-ASCII"),"US-ASCII");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }  
// try {
//     // Convert a string to ISO-LATIN-1 bytes in a ByteBuffer
//     // The new ByteBuffer is ready to be read.
//     ByteBuffer bbuf = encoder.encode(CharBuffer.wrap("a string"));

//     // Convert ISO-LATIN-1 bytes in a ByteBuffer to a character ByteBuffer and then to a string.
//     // The new ByteBuffer is ready to be read.
//     CharBuffer cbuf = decoder.decode(bbuf);
//     String s = cbuf.toString();
// } catch (CharacterCodingException e) {
// }
  }
}
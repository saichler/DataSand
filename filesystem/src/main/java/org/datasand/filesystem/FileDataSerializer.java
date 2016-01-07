package org.datasand.filesystem;

import org.datasand.codec.ISerializer;
import org.datasand.codec.bytearray.BytesArray;

public class FileDataSerializer implements ISerializer{

	@Override
	public void encode(Object value, byte[] byteArray, int location) {
	}

	@Override
	public void encode(Object value, EncodeDataContainer ba) {
		FileData.encode(value, (BytesArray)ba);
	}

	@Override
	public Object decode(byte[] byteArray, int location, int length) {
		return null;
	}

	@Override
	public Object decode(EncodeDataContainer ba, int length) {
		return FileData.decode((BytesArray)ba, length);
	}

	@Override
	public String getShardName(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getRecordKey(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}
}

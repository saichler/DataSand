package org.datasand.filesystem;

import org.datasand.codec.BytesArray;
import org.datasand.codec.serialize.ISerializer;

public class FileDataSerializer implements ISerializer{

	@Override
	public void encode(Object value, BytesArray ba) {
		FileData.encode(value, ba);
	}

	@Override
	public Object decode(BytesArray ba) {
		return FileData.decode(ba);
	}

}

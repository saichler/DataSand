package org.datasand.filesystem;

import java.io.IOException;
import org.datasand.codec.BytesArray;
import org.datasand.codec.serialize.ISerializer;

public class FileDataSerializer implements ISerializer{

	@Override
	public void encode(Object value, BytesArray ba) {
		try {
			FileData.encode(value, ba);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object decode(BytesArray ba) {
		return FileData.decode(ba);
	}

}

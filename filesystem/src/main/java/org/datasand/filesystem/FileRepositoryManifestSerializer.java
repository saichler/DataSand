package org.datasand.filesystem;

import org.datasand.codec.BytesArray;
import org.datasand.codec.serialize.ISerializer;

public class FileRepositoryManifestSerializer implements ISerializer{

	@Override
	public void encode(Object value, BytesArray ba) {
		FileRepositoryManifest.encode(value, ba);
	}

	@Override
	public Object decode(BytesArray ba) {
		return FileRepositoryManifest.decode(ba);
	}

}

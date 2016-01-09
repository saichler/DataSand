package org.datasand.filesystem;

import org.datasand.codec.serialize.ISerializer;
import org.datasand.codec.BytesArray;

public class FileRepositoryManifestSerializer implements ISerializer{

	@Override
	public void encode(Object value, byte[] byteArray, int location) {
	}

	@Override
	public void encode(Object value, EncodeDataContainer ba) {
		FileRepositoryManifest.encode(value, (BytesArray)ba);
	}

	@Override
	public Object decode(byte[] byteArray, int location, int length) {
		return null;
	}

	@Override
	public Object decode(EncodeDataContainer ba, int length) {
		return FileRepositoryManifest.decode((BytesArray)ba);
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

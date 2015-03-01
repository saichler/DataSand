package org.datasand.filesystem;

import org.datasand.codec.EncodeDataContainer;
import org.datasand.codec.ISerializer;
import org.datasand.codec.bytearray.ByteArrayEncodeDataContainer;

public class FileRepositoryManifestSerializer implements ISerializer{

	@Override
	public void encode(Object value, byte[] byteArray, int location) {
	}

	@Override
	public void encode(Object value, EncodeDataContainer ba) {
		FileRepositoryManifest.encode(value, (ByteArrayEncodeDataContainer)ba);
	}

	@Override
	public Object decode(byte[] byteArray, int location, int length) {
		return null;
	}

	@Override
	public Object decode(EncodeDataContainer ba, int length) {
		return FileRepositoryManifest.decode((ByteArrayEncodeDataContainer)ba);
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

package bench;

import com.google.protobuf.ByteString;

import crypto.CryptographicDigest;
import serialization.MptSerialization;

public class SerializationSizeBenchmark {
	
	private static final byte[] FOUR_BYTES_ONES = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
	private static final byte[] FOUR_BYTES_ZEROS = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
	
	
	public static void main(String[] args) {
		
		/** 
		 * Benchmark sizes of different types of protobuf messages 
		 */
		
		MptSerialization.Node leafones = 
				MptSerialization.Node.newBuilder()
				.setLeaf(
					MptSerialization.Leaf.newBuilder()
					.setKey(ByteString.copyFrom(FOUR_BYTES_ONES))
					.setValue(ByteString.copyFrom(FOUR_BYTES_ONES))
					.build())
				.build();
		System.out.println("Size of a ones leaf (# bytes): "+leafones.toByteArray().length);

		MptSerialization.Node leafzeros = 
				MptSerialization.Node.newBuilder()
				.setLeaf(
					MptSerialization.Leaf.newBuilder()
					.setKey(ByteString.copyFrom(FOUR_BYTES_ZEROS))
					.setValue(ByteString.copyFrom(FOUR_BYTES_ZEROS))
					.build())
				.build();
		System.out.println("Size of a zeros leaf (# bytes): "+leafzeros.toByteArray().length);
		
		MptSerialization.Node stubA = 
				MptSerialization.Node.newBuilder()
				.setStub(
					MptSerialization.Stub.newBuilder()
						.setHash(ByteString.copyFrom(CryptographicDigest.hash("SOME STRING".getBytes())))
						.build())
				.build();
		System.out.println("Size of a stub (A) (# bytes): "+stubA.toByteArray().length);

		MptSerialization.Node stubB = 
				MptSerialization.Node.newBuilder()
				.setStub(
					MptSerialization.Stub.newBuilder()
						.setHash(ByteString.copyFrom(CryptographicDigest.hash("OTHER STRING".getBytes())))
						.build())
				.build();
		System.out.println("Size of a stub (B) (# bytes): "+stubB.toByteArray().length);
		
		MptSerialization.Node interiorNodeEmpty = 
				MptSerialization.Node.newBuilder()
					.setInteriorNode(MptSerialization.InteriorNode
							.newBuilder().build())
					.build();
		System.out.println("Size of an interior node empty (# bytes): "+
				interiorNodeEmpty.toByteArray().length);
		
		MptSerialization.Node interiorNodeOneStub = 
				MptSerialization.Node.newBuilder().setInteriorNode(
						MptSerialization.InteriorNode.newBuilder()
						.setLeft(stubA)
						.build())
				.build();
		System.out.println("Size of an interior node with one stub (# bytes): "+
				interiorNodeOneStub.toByteArray().length);
		
		MptSerialization.Node interiorNodeOneLeaf = 
				MptSerialization.Node.newBuilder().setInteriorNode(
						MptSerialization.InteriorNode.newBuilder()
						.setLeft(leafones)
						.build())
				.build();
		System.out.println("Size of an interior node with one leaf (# bytes): "+
				interiorNodeOneLeaf.toByteArray().length);

		MptSerialization.Node interiorNodeTwoStubs = 
				MptSerialization.Node.newBuilder().setInteriorNode(
						MptSerialization.InteriorNode.newBuilder()
						.setLeft(stubA)
						.setRight(stubB)
						.build())
				.build();
		System.out.println("Size of an interior node with two stubs (# bytes): "
				+interiorNodeTwoStubs.toByteArray().length);

		MptSerialization.Node interiorNodeTwoLeaves = 
				MptSerialization.Node.newBuilder().setInteriorNode(
						MptSerialization.InteriorNode.newBuilder()
						.setLeft(stubA)
						.setRight(stubB)
						.build())
				.build();
		System.out.println("Size of an interior node with two leaves (# bytes): "
				+interiorNodeTwoLeaves.toByteArray().length);
		
		MptSerialization.Node interiorNodeOneInterior = 
				MptSerialization.Node.newBuilder().setInteriorNode(
						MptSerialization.InteriorNode.newBuilder()
						.setLeft(interiorNodeOneStub)
						.build())
				.build();
		System.out.println("Size of an interior node with one interior (# bytes): "
				+interiorNodeOneInterior.toByteArray().length);
		
		MptSerialization.Node interiorNodeTwoInteriorA = 
				MptSerialization.Node.newBuilder().setInteriorNode(
						MptSerialization.InteriorNode.newBuilder()
						.setLeft(interiorNodeOneStub)
						.setRight(interiorNodeOneLeaf)
						.build())
				.build();
		System.out.println("Size of an interior node with two interior A (# bytes): "
				+interiorNodeTwoInteriorA.toByteArray().length);
		
		MptSerialization.Node interiorNodeTwoInteriorB = 
				MptSerialization.Node.newBuilder().setInteriorNode(
						MptSerialization.InteriorNode.newBuilder()
						.setLeft(interiorNodeTwoStubs)
						.setRight(interiorNodeTwoLeaves)
						.build())
				.build();
		System.out.println("Size of an interior node with two interior B (# bytes): "
				+interiorNodeTwoInteriorB.toByteArray().length);		
	}
	
}
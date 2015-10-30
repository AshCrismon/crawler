package pers.ash.bdb.examples;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class BDBEnvTest {

	private String envHome = "D:/Documents/Databases/BerkeleyDB";
	private Database vendorDb;
	private Database classDb;
	private ClassCatalog classCatalog;
	private EntryBinding<Vendor> dataBinding;

	@Before
	public void init(){
		BDBEnv.setup(new File(envHome));
		vendorDb = BDBEnv.openDatabase("vendor");
		classDb = BDBEnv.openClassDatabase("classDb");
		classCatalog = BDBEnv.getClassCatalog();
		dataBinding = new SerialBinding<Vendor>(classCatalog, Vendor.class);
	}
	
	@Test
	public void testWriteToVendorDb() {
		
		writeDataToVendorDb(vendorDb, classCatalog);
		vendorDb.sync();	//这个方法只在设置延迟写的时候有效，执行过后会写磁盘，默认情况下，关闭数据库时会写磁盘
		classDb.sync();
		BDBEnv.closeDatabase(vendorDb);
	}
	
	@Test
	public void testReadFromVendorDb() throws UnsupportedEncodingException {
		readDataFromVendorDb(vendorDb, classCatalog);
		BDBEnv.closeDatabase(vendorDb);
	}
	
	@Test
	public void testDeleteFromVendorDb() throws UnsupportedEncodingException{
		String theKey = "The Pantry";
		DatabaseEntry key = new DatabaseEntry(theKey.getBytes("UTF-8"));
		OperationStatus status = vendorDb.delete(null, key);
		assertEquals(OperationStatus.SUCCESS, status);
		
		DatabaseEntry data = new DatabaseEntry();
		vendorDb.get(null, key, data, LockMode.DEFAULT);
		
		assertNull(data.getData());//这里不sync或close，结果不会写到磁盘
	}
	
	@Test
	public void testCursor() throws UnsupportedEncodingException{
		
		Cursor cursor = vendorDb.openCursor(null, null);//打开数据库的游标
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		System.out.println("=============Iterate from the first record to the last record=============");
		while(cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS){
			String theKey = new String(key.getData(), "UTF-8");
			Vendor theData = dataBinding.entryToObject(data);
			System.out.println("key--------->" + theKey);
			System.out.println("data--------->" + theData.toString());
		}
		cursor.close();
	}
	
	@Test
	public void testCursor2() throws UnsupportedEncodingException{
		
		Cursor cursor = vendorDb.openCursor(null, null);//打开数据库的游标
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		System.out.println("=============Iterate from the last record to the first record=============");
		while(cursor.getPrev(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS){
			String theKey = new String(key.getData(), "UTF-8");
			Vendor theData = dataBinding.entryToObject(data);
			System.out.println("key--------->" + theKey);
			System.out.println("data--------->" + theData.toString());
		}
		cursor.close();
	}
	
	@Test
	public void testCursor3() throws Exception{
		Cursor cursor = vendorDb.openCursor(null, null);
		DatabaseEntry key = new DatabaseEntry("Simply Fresh".getBytes());
		DatabaseEntry data = new DatabaseEntry();
		OperationStatus status = cursor.getSearchKey(key, data, LockMode.DEFAULT);
		assertEquals(OperationStatus.SUCCESS, status);
		cursor.close();
	}
	
	@Test
	public void testCursor4() throws Exception{
		Cursor cursor = vendorDb.openCursor(null, null);
		DatabaseEntry key = new DatabaseEntry("Simply".getBytes());
		DatabaseEntry data = new DatabaseEntry();
		OperationStatus status = cursor.getSearchKey(key, data, LockMode.DEFAULT);
		assertEquals(OperationStatus.NOTFOUND, status);
		cursor.close();
	}

	public void writeDataToVendorDb(Database vendorDb, ClassCatalog classCatalog) {
		List<String[]> records = loadFile("vendors.txt", 8);
		for(int i = 0; i < records.size(); i++){
			String[] fieldArray = records.get(i);
			Vendor vendor = new Vendor();
            vendor.setVendorName(fieldArray[0]);
            vendor.setAddress(fieldArray[1]);
            vendor.setCity(fieldArray[2]);
            vendor.setState(fieldArray[3]);
            vendor.setZipcode(fieldArray[4]);
            vendor.setBusinessPhoneNumber(fieldArray[5]);
            vendor.setRepName(fieldArray[6]);
            vendor.setRepPhoneNumber(fieldArray[7]);
            
            try {
				DatabaseEntry key = new DatabaseEntry(vendor.getVendorName().getBytes("UTF-8"));
				DatabaseEntry data = new DatabaseEntry();
				dataBinding.objectToEntry(vendor, data);
				vendorDb.put(null, key, data);
            } catch (UnsupportedEncodingException e) {
				System.err.println(e.getMessage());
			}
		}
	}
	
	public void readDataFromVendorDb(Database vendorDb, ClassCatalog classCatalog) throws UnsupportedEncodingException{
		String theKey = "The Pantry";
		DatabaseEntry key = new DatabaseEntry(theKey.getBytes("UTF-8"));
		DatabaseEntry data = new DatabaseEntry();
		OperationStatus status = vendorDb.get(null, key, data, LockMode.DEFAULT);
		if(status == OperationStatus.SUCCESS){
			vendorDb.get(null, key, data, LockMode.DEFAULT);
			Vendor theData = dataBinding.entryToObject(data);
			System.out.println(theData.toString());
		}
	}

	private List<String[]> loadFile(String fileName, int numFields) {
		String path = this.getClass().getResource("").getPath();
		File file = FileUtils.getFile(path + fileName);
		List<String> lines = Collections.<String>emptyList();
		try {
			lines = FileUtils.readLines(file, "UTF-8");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		List<String[]> records = new ArrayList<String[]>();
		
		for(int i = 0; i < lines.size(); i++){
			String[] fieldArray = lines.get(i).split("#");
			 if (fieldArray.length != numFields){
				 System.out.println("Malformed line found in " + file.getPath());
                 System.out.println("Line was: '" + fieldArray);
                 System.out.println("length found was: " + fieldArray.length);
                 System.exit(-1);
			 }
			records.add(fieldArray);
		}
		return records;
	}
}

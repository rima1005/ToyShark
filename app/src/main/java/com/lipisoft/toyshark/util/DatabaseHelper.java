package com.lipisoft.toyshark.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "ToyShark.Util.Database";

    private static final String DB_NAME = "ToyShark.db";
    private static final int DB_VERSION = 1;

    private static boolean once = true;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    private static DatabaseHelper dh = null;

    public static DatabaseHelper getInstance(Context context)
    {
        if(dh == null)
            dh = new DatabaseHelper(context.getApplicationContext());
        return dh;
    }

    @Override
    public synchronized void close() {
        Log.w(TAG, "Database is being closed!");
    }

    private DatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);

        if(once)
        {
            File dbfile = context.getDatabasePath(DB_NAME);
            if (dbfile.exists()) {
                Log.w(TAG, "Deleting " + dbfile);
                dbfile.delete();
            }

            File dbjournal = context.getDatabasePath(DB_NAME + "-journal");
            if (dbjournal.exists()) {
                Log.w(TAG, "Deleting " + dbjournal);
                dbjournal.delete();
            }
        }


    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.i(TAG, "Creating database " + DB_NAME + " version " + DB_VERSION);
        createTableHosts(db);
        createTablePackets(db);
        createTableSuites(db);
        createTableProtocols(db);
        createTableSupportedSuites(db);
        createTableSupportedProtocols(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        db.enableWriteAheadLogging();
        super.onConfigure(db);
    }

    private void createTableHosts(SQLiteDatabase db)
    {
        Log.i(TAG, "Creating hosts table");
        db.execSQL("CREATE TABLE hosts ("+
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", host INTEGER NOT NULL" +
                ", port INTEGER NOT NULL" +
                ", hostname TEXT" +
                ", protocol TEXT" +
                ", suite TEXT" +
                ", queryCounter INTEGER DEFAULT 0" +
                ");");
        db.execSQL("CREATE UNIQUE INDEX idx_host ON hosts(host, port)");
    }

    private void createTablePackets(SQLiteDatabase db)
    {
        Log.i(TAG, "Creating packets table");
        db.execSQL("CREATE TABLE packets(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", host INTEGER NOT NULL" +
                ", port INTEGER NOT NULL" +
                ", protocol INTEGER" +
                ", length INTEGER" +
                ", data TEXT" +
                ");");

    }

    private void createTableSuites(SQLiteDatabase db)
    {
        Log.i(TAG, "Creating suites table");
        db.execSQL("CREATE TABLE suites(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", name TEXT NOT NULL" +
                ");");

        db.execSQL("CREATE UNIQUE INDEX idx_suite ON suites(name)");
    }

    private void createTableProtocols(SQLiteDatabase db)
    {
        Log.i(TAG, "Creating protocols table");
        db.execSQL("CREATE TABLE protocols(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", name TEXT NOT NULL" +
                ");");

        db.execSQL("CREATE UNIQUE INDEX idx_protocol ON protocols(name)");
    }

    private void createTableSupportedSuites(SQLiteDatabase db)
    {
        Log.i(TAG, "Creating supportedSuites table");
        db.execSQL("CREATE TABLE supportedSuites(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", hostID INTEGER NOT NULL" +
                ", suiteID INTEGER NOT NULL" +
                ");");

        db.execSQL("CREATE UNIQUE INDEX idx_supportedSuite ON supportedSuites(hostID, suiteID)");
    }

    private void createTableSupportedProtocols(SQLiteDatabase db)
    {
        Log.i(TAG, "Creating supportedProtocols table");
        db.execSQL("CREATE TABLE supportedProtocols(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", hostID INTEGER NOT NULL" +
                ", protocolID INTEGER NOT NULL" +
                ");");

        db.execSQL("CREATE UNIQUE INDEX idx_supportedProtocol ON supportedProtocols(hostID, protocolID)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.i(TAG, DB_NAME + "upgrading from version " + oldVersion + " to " + newVersion);

        db.beginTransaction();
        try{


            if(oldVersion == DB_VERSION)
            {
                db.setVersion(oldVersion);
                db.setTransactionSuccessful();
                Log.i(TAG, DB_NAME + "upgraded to " + DB_VERSION);
            }
            else
            {
                throw new IllegalArgumentException(DB_NAME + " upgraded to " + oldVersion + " but required " + DB_VERSION);
            }
        } catch (Throwable ex)
        {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }
        finally {
            db.endTransaction();
        }
    }

    public void insertHost(int daddr, int port, String hostname)
    {
        insertHost(daddr, port, hostname, null ,null);
    }

    public void insertHost(int daddr, int port, String hostname, String cipherSuite, String protocol)
    {
        lock.writeLock().lock();
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransactionNonExclusive();
            try
            {
                ContentValues cv = new ContentValues();
                cv.put("host", daddr);
                cv.put("port", port);
                cv.put("hostname", hostname);
                if(cipherSuite != null)
                    cv.put("suite", cipherSuite);
                if(protocol != null)
                    cv.put("protocol", protocol);
                cv.put("queryCounter", 1);

                if(db.insert("hosts", null, cv) == -1)
                    Log.e(TAG, "Insert Host failed!");

                db.setTransactionSuccessful();
            }finally {
                db.endTransaction();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void increaseCounter(int daddr, int port)
    {
        lock.writeLock().lock();
        lock.readLock().lock();
        try
        {
            SQLiteDatabase dbr = getReadableDatabase();
            int counter = -1;
            Cursor c = dbr.rawQuery("SELECT queryCounter FROM hosts WHERE host = ? AND port = ?", new String[]{String.valueOf(daddr), String.valueOf(port)});
            if(c.moveToFirst())
                counter = c.getInt(c.getColumnIndex("queryCounter"));
            c.close();
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransactionNonExclusive();
            try
            {
                if(counter > -1)
                {
                    ContentValues cv = new ContentValues();
                    cv.put("queryCounter", counter + 1);
                    db.update("hosts", cv, "host = ? AND port = ?", new String[]{String.valueOf(daddr), String.valueOf(port)});
                    db.setTransactionSuccessful();
                }

            } finally {
                db.endTransaction();
            }
        }finally {
            lock.readLock().unlock();
            lock.writeLock().unlock();
        }
    }

    public void insertProtocol(String protocol)
    {
        lock.writeLock().lock();
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransactionNonExclusive();
            try{
                ContentValues cv = new ContentValues();
                cv.put("name", protocol);

                if(db.insert("protocols", null, cv) == -1)
                    Log.e(TAG, "Could not insert Protocol");

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }finally {
            lock.writeLock().unlock();
        }
    }

    public void insertSuite(String suite)
    {
        lock.writeLock().lock();
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransactionNonExclusive();
            try{
                ContentValues cv = new ContentValues();
                cv.put("name", suite);

                if(db.insert("suites", null, cv) == -1)
                    Log.e(TAG, "Could not insert Suite");

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }finally {
            lock.writeLock().unlock();
        }
    }

    public void insertSupportedProtocol(int HostId, int protocolId)
    {
        lock.writeLock().lock();
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransactionNonExclusive();
            try{
                ContentValues cv = new ContentValues();
                cv.put("hostID", HostId);
                cv.put("protocolID", protocolId);

                if(db.insert("supportedProtocols", null, cv) == -1)
                    Log.e(TAG, "Could not insert supported protocol");

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }finally {
            lock.writeLock().unlock();
        }
    }

    public void insertSupportedSuite(int HostId, int suiteId)
    {
        lock.writeLock().lock();
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransactionNonExclusive();
            try{
                ContentValues cv = new ContentValues();
                cv.put("hostID", HostId);
                cv.put("suiteID", suiteId);

                if(db.insert("supportedSuites", null, cv) == -1)
                    Log.e(TAG, "Could not insert supported suit");

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }finally {
            lock.writeLock().unlock();
        }
    }

    public Cursor getHost(int addr, int port)
    {
        lock.readLock().lock();
        try{
            SQLiteDatabase db = getReadableDatabase();
            String queryString = "SELECT * FROM hosts WHERE host = ? OR hostname = ? AND port = " + port;
            return db.rawQuery(queryString, new String[]{String.valueOf(addr), String.valueOf(addr)});
        }finally {
            lock.readLock().unlock();
        }
    }

    public Cursor getSuite(String name)
    {
        lock.readLock().lock();
        try {
            SQLiteDatabase db = getReadableDatabase();
            String queryString = "SELECT * FROM suites WHERE name = ?";
            return db.rawQuery(queryString, new String[]{name});
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean suiteExists(String name)
    {
        lock.readLock().lock();
        boolean exists = false;
        try{
            SQLiteDatabase db = getReadableDatabase();
            String queryString = "SELECT COUNT(name) as existing FROM suites WHERE name = ?";
            Cursor c = db.rawQuery(queryString, new String[]{name});
            if(!c.moveToFirst())
                exists = false;
            else
            {
                int count = c.getInt(c.getColumnIndex("existing"));
                if(count <= 0)
                    exists = false;
                else
                    exists = true;
            }
        } finally {
            lock.readLock().unlock();

        }
        return exists;
    }

    public boolean protocolExists(String name)
    {
        lock.readLock().lock();
        boolean exists = false;
        try{
            SQLiteDatabase db = getReadableDatabase();
            String queryString = "SELECT COUNT(name) as existing FROM protocols WHERE name = ?";
            Cursor c = db.rawQuery(queryString, new String[]{name});
            if(!c.moveToFirst())
                exists = false;
            else
            {
                int count = c.getInt(c.getColumnIndex("existing"));
                if(count <= 0)
                    exists = false;
                else
                    exists = true;
            }
        } finally {
            lock.readLock().unlock();

        }
        return exists;
    }

    public boolean hostexists(int host, int port)
    {
        lock.readLock().lock();
        boolean exists = false;
        try{
            SQLiteDatabase db = getReadableDatabase();
            String queryString = "SELECT COUNT(host) as existing FROM hosts WHERE host = ? AND port = " + port;
            Cursor c = db.rawQuery(queryString, new String[]{String.valueOf(host)});
            if(!c.moveToFirst())
                exists = false;
            else
            {
                int count = c.getInt(c.getColumnIndex("existing"));
                if(count <= 0)
                    exists = false;
                else
                    exists = true;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            lock.readLock().unlock();

        }
        return exists;
    }

    public void updateHost(int host, int port, String suite, String protocol)
    {
        lock.writeLock().lock();
        try
        {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("suite", suite);
            cv.put("protocol", protocol);

            int modrows = db.update("hosts", cv, "host = ? AND port = ?", new String[]{String.valueOf(host), String.valueOf(port)});
            Log.i(TAG, "Modified : " + modrows + " Rows");
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public Cursor getProtocol(String name)
    {
        lock.readLock().lock();
        try {
            SQLiteDatabase db = getReadableDatabase();
            String queryString = "SELECT * FROM protocols WHERE name = ?";
            return db.rawQuery(queryString, new String[]{name});
        } finally {
            lock.readLock().unlock();
        }
    }

    public Cursor getSupportedSuites(int hostId)
    {
        lock.readLock().lock();
        try {
            SQLiteDatabase db = getReadableDatabase();
            String queryString = "SELECT ss.suiteID, s.name FROM supportedSuites as ss INNER JOIN suites AS s ON s.ID = ss.suiteID WHERE hostID = " + hostId;
            return db.rawQuery(queryString, new String[]{});
        } finally {
            lock.readLock().unlock();
        }
    }

    public Cursor getSupportedProtocols(int hostId)
    {
        lock.readLock().lock();
        try {
            SQLiteDatabase db = getReadableDatabase();
            String queryString = "SELECT sp.protocolID, p.name FROM supportedProtocols as sp INNER JOIN protocols p ON p.ID = sp.protocolID WHERE hostID = " + hostId;
            return db.rawQuery(queryString, new String[]{});
        } finally {
            lock.readLock().unlock();
        }
    }

    public void insertPacket(int host, int port, int protocol, int length, String data)
    {

        lock.writeLock().lock();
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransactionNonExclusive();
            try
            {
                ContentValues cv = new ContentValues();
                cv.put("host", host);
                cv.put("port", port);
                cv.put("protocol", protocol);
                cv.put("length", length);
                cv.put("data", data);

                if(db.insert("packets", null, cv) == -1)
                    Log.e(TAG, "Could not insert packet!");
                db.setTransactionSuccessful();
            }finally {
                db.endTransaction();
            }

        }finally {
            lock.writeLock().unlock();
        }
    }

    public Cursor getHosts()
    {
        lock.readLock().lock();
        Cursor cursor;
        try
        {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery("SELECT host, hostname, port, queryCounter FROM hosts", new String[]{});
        } finally {
            lock.readLock().unlock();

        }
        return cursor;
    }

    public Cursor getPackets(int host, int port)
    {
        lock.readLock().lock();
        Cursor cursor;
        try
        {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM packets WHERE host = " + host + " AND port = " + port, new String[]{});
        } finally {
            lock.readLock().unlock();

        }
        return cursor;
    }

    public Cursor getFilteredPackets(int host, int port, String Keyword)
    {
        lock.readLock().lock();
        Cursor cursor;
        try
        {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM packets WHERE host = " + host + " AND port = " + port + " AND data LIKE ?" , new String[]{"%" + Keyword + "%"});
        } finally {
            lock.readLock().unlock();

        }
        return cursor;
    }

    public String getRequest(int id)
    {
        lock.readLock().lock();
        String request = "Request not found!";
        try
        {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM packets WHERE ID = " + id , new String[]{});
            if(cursor.moveToFirst())
                request = cursor.getString(cursor.getColumnIndex("data"));
            cursor.close();
        } finally {
            lock.readLock().unlock();

        }
        return request;
    }


}

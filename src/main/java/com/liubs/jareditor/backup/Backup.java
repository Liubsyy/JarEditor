package com.liubs.jareditor.backup;

import com.liubs.jareditor.constant.PathConstant;
import com.liubs.jareditor.persistent.BackupStorage;
import com.liubs.jareditor.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Liubsyy
 * @date 2025/5/23
 */
public class Backup {

    private String backupBaseDir;

    public Backup(String backupBaseDir) {
        this.backupBaseDir = backupBaseDir;
    }
    public Backup() {
        this.backupBaseDir = BackupStorage.getInstance().getBackupPath();
    }

    private String uniqueBackupPath(String jarName,String jarPath) {
        return jarName+"_"+Md5Util.md5(jarPath);
    }

    /**
     * 备份路径：{user.home}/jareditor_backup/jar路径的md5值/时间/[具体jar和change.json]
     *
     * @param jarPath
     * @param changeData
     */
    public void backupJar(String jarPath,ChangeData changeData){
        String jarName = MyPathUtil.getSingleFileName(jarPath);
        String jarMd5Path = uniqueBackupPath(jarName, Md5Util.md5(jarPath));

        //路径目录+md5(jar)
        File baseDestDir = new File(backupBaseDir,jarMd5Path);
        if( !baseDestDir.exists() ) {
            baseDestDir.mkdirs();
        }

        //具体版本目录
        String now = changeData.getCreateTime().replaceAll(":","_");    //路径不能存:改成_
        File versionDir = new File(baseDestDir.getPath(),now);
        if( !versionDir.exists() ){
            versionDir.mkdirs();
        }

        //jar和change.json
        File destJar = new File(versionDir.getPath(),jarName);
        try (FileInputStream tempInputStream = new FileInputStream(jarPath);
             FileOutputStream fileOutputStream = new FileOutputStream(destJar)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = tempInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            byte[] changeBytes = JsonUtil.toJson(changeData).getBytes(StandardCharsets.UTF_8);
            Files.write(Paths.get(versionDir.getPath(),PathConstant.BACKUP_CHANGE_JSON),changeBytes);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 一个jar没有任何备份时，备份第一个版本
     * @param jarPath
     */
    public void checkBackupFirstVersion(String jarPath) {
        String jarName = MyPathUtil.getSingleFileName(jarPath);
        String jarMd5Path = uniqueBackupPath(jarName, Md5Util.md5(jarPath));

        //路径目录+md5(jar)
        File baseDestDir = new File(backupBaseDir,jarMd5Path);
        if( !baseDestDir.exists() ) {
            baseDestDir.mkdirs();
        }

        try {
            boolean existJson = Files.walk(baseDestDir.toPath())
                    .filter(Files::isRegularFile)
                    .anyMatch(path -> path.getFileName().toString().equals(PathConstant.BACKUP_CHANGE_JSON));
            if(existJson){
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ChangeData changeData = new ChangeData();
        changeData.setCreateTime(DateUtil.formatDate(DateUtil.addSecond(new Date(),-1)));  //错开一秒
        changeData.setChangeList(new ArrayList<>());
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                ChangeItem changeItem = new ChangeItem();
                changeItem.setChangeType(ChangeType.ADD.value);
                changeItem.setEntry(entry.getName());
                changeData.getChangeList().add(changeItem);
            }
        }catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        this.backupJar(jarPath, changeData);
    }

    public List<BackupData> getBackupData(String jarPath) {
        List<BackupData> backupDataList = new ArrayList<>();
        String jarName = MyPathUtil.getSingleFileName(jarPath);
        String jarMd5Path = uniqueBackupPath(jarName, Md5Util.md5(jarPath));

        File baseDestDir = new File(backupBaseDir,jarMd5Path);
        if( !baseDestDir.exists() ) {
            return backupDataList;
        }

        File[] subFiles = baseDestDir.listFiles();
        if(null != subFiles) {
           for(File subFile : subFiles) {
               if(subFile.isDirectory()) {
                   File destJar = new File(subFile.getPath(),jarName);
                   if(destJar.exists()) {
                       Path changeFile = Paths.get(subFile.getPath(), PathConstant.BACKUP_CHANGE_JSON);
                       if(changeFile.toFile().exists()) {
                           try {
                               String json = Files.readString(changeFile, StandardCharsets.UTF_8);
                               ChangeData changeData = JsonUtil.parse(json, ChangeData.class);
                               if(StringUtils.isNotEmpty(changeData.getCreateTime())){
                                   BackupData backupData = new BackupData();
                                   backupData.setBackupJar(destJar.getAbsolutePath());
                                   backupData.setChangeData(changeData);
                                   backupData.setCreateTime(DateUtil.parseDate(changeData.getCreateTime()));
                                   backupDataList.add(backupData);
                               }
                           } catch (Exception ex) {
                               ex.printStackTrace();
                           }
                       }

                   }
               }
           }
        }

        //按时间倒序
        backupDataList.sort((o1, o2) -> Long.compare(o2.getCreateTime().getTime(), o1.getCreateTime().getTime()));
        return backupDataList;
    }

    public ChangeData getChangeDataFromDir(String jarEditDir) throws IOException {
        Path jareditoutPath = Paths.get(jarEditDir);
        List<String> modifyEntries = new ArrayList<>();
        Files.walk(jareditoutPath)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    String relativePath = jareditoutPath.relativize(path).toString().replace("\\", "/");
                    modifyEntries.add(relativePath);
                });
        ChangeData changeData = new ChangeData();
        changeData.setCreateTime(DateUtil.formatDate(new Date()));
        changeData.setChangeList(new ArrayList<>());
        for(String modifyEntry : modifyEntries) {
            ChangeItem changeItem = new ChangeItem();
            changeItem.setChangeType(ChangeType.MODIFY.value);
            changeItem.setEntry(modifyEntry);
            changeData.getChangeList().add(changeItem);
        }
        return changeData;
    }

}

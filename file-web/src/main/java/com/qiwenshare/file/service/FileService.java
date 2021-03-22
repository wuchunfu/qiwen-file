package com.qiwenshare.file.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.qiwenshare.common.operation.FileOperation;
import com.qiwenshare.common.oss.AliyunOSSDelete;
import com.qiwenshare.common.util.FileUtil;
import com.qiwenshare.common.util.PathUtil;
import com.qiwenshare.file.api.IFileService;
import com.qiwenshare.common.config.QiwenFileConfig;
import com.qiwenshare.file.domain.FileBean;
import com.qiwenshare.file.mapper.FileMapper;
import com.qiwenshare.file.mapper.UserFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class FileService extends ServiceImpl<FileMapper, FileBean> implements IFileService {

    @Resource
    FileMapper fileMapper;
    @Resource
    UserFileMapper userFileMapper;
    @Resource
    FiletransferService filetransferService;
    @Resource
    QiwenFileConfig qiwenFileConfig;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;

//    @Override
//    public void batchInsertFile(List<FileBean> fileBeanList, Long userId) {
//        StorageBean storageBean = filetransferService.selectStorageBean(new StorageBean(userId));
//        long fileSizeSum = 0;
//        for (FileBean fileBean : fileBeanList) {
//            if (fileBean.getIsDir() == 0) {
//                fileSizeSum += fileBean.getFileSize();
//            }
//        }
//        fileMapper.batchInsertFile(fileBeanList);
//        if (storageBean != null) {
//            long updateFileSize = storageBean.getStorageSize() + fileSizeSum;
//
//            storageBean.setStorageSize(updateFileSize);
//            filetransferService.updateStorageBean(storageBean);
//        }
//    }

    @Override
    public void increaseFilePointCount(Long fileId) {
        FileBean fileBean = fileMapper.selectById(fileId);
        fileBean.setPointCount(fileBean.getPointCount()+1);
        fileMapper.updateById(fileBean);
    }

    @Override
    public void decreaseFilePointCount(Long fileId) {
        FileBean fileBean = fileMapper.selectById(fileId);
        fileBean.setPointCount(fileBean.getPointCount()-1);
        fileMapper.updateById(fileBean);
    }

//    @Override
//    public void updateFile(FileBean fileBean) {
//        fileBean.setUploadTime(DateUtil.getCurrentTime());
//        fileMapper.updateFile(fileBean);
//    }




//    @Override
//    public List<FileBean> selectFileListByPath(FileBean fileBean) {
//        LambdaQueryWrapper<FileBean> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(FileBean::getFilePath, fileBean.getFilePath())
//                .eq(FileBean::getUserId, fileBean.getUserId())
//                .orderByDesc(FileBean::getIsDir);
//        return fileMapper.selectList(lambdaQueryWrapper);
//    }
    @Override
    public void deleteLocalFile(FileBean fileBean) {
        log.info("删除本地文件：" + JSON.toJSONString(fileBean));
        //删除服务器文件
        if (fileBean.getFileUrl() != null && fileBean.getFileUrl().indexOf("upload") != -1){
            if (fileBean.getIsOSS() != null && fileBean.getIsOSS() == 1) {
                AliyunOSSDelete.deleteObject(qiwenFileConfig.getAliyun().getOss(), fileBean.getFileUrl().substring(1));
            } else if (fileBean.getStorageType() == 0) {
                FileOperation.deleteFile(PathUtil.getStaticPath() + fileBean.getFileUrl());
                if (FileUtil.isImageFile(FileUtil.getFileExtendName(fileBean.getFileUrl()))) {
                    FileOperation.deleteFile(PathUtil.getStaticPath() + fileBean.getFileUrl().replace(fileBean.getTimeStampName(), fileBean.getTimeStampName() + "_min"));
                }
            } else if (fileBean.getStorageType() == 1) {
                AliyunOSSDelete.deleteObject(qiwenFileConfig.getAliyun().getOss(), fileBean.getFileUrl().substring(1));
            } else if (fileBean.getStorageType() == 2){
                fastFileStorageClient.deleteFile(fileBean.getFileUrl());

            } else {
                FileOperation.deleteFile(PathUtil.getStaticPath() + fileBean.getFileUrl());
                if (FileUtil.isImageFile(FileUtil.getFileExtendName(fileBean.getFileUrl()))) {
                    FileOperation.deleteFile(PathUtil.getStaticPath() + fileBean.getFileUrl().replace(fileBean.getTimeStampName(), fileBean.getTimeStampName() + "_min"));
                }
            }
        }
    }






}

package com.hunofox.pictureselector.mvp.presenters;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import com.hunofox.pictureselector.base.PictureSelectorPresenter;
import com.hunofox.pictureselector.beans.FolderBean;
import com.hunofox.pictureselector.beans.PictureBean;
import com.hunofox.pictureselector.mvp.views.IPictureView;
import com.hunofox.pictureselector.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.hunofox.pictureselector.base.PictureSelector.CONTEXT;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/7/26 14:11.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */
public class PicturePresenter extends PictureSelectorPresenter<IPictureView> {

    private static final int LOAD_ALL = 550;
    private static final int LOAD_PATH = 697;
    public boolean isFirstInit = true;//是否是初次初始化图片加载器

    private final List<PictureBean> datas = new ArrayList<>();
    private final List<FolderBean> folders = new ArrayList<>();

    public PicturePresenter(IPictureView view) {
        super(view);
    }

    /**
     * 图片数据加载器
     *
     * @param loaderManager 可通过FragmentActivity.getSupportLoaderManager获取
     *
     * 特别注意：若在Fragment中使用，必须在onActivityCreated()方法中调用
     */
    public void initPicturesData(LoaderManager loaderManager, String path){
        if(path == null || TextUtils.isEmpty(path.trim())){
            loaderManager.restartLoader(LOAD_ALL, null, loaderCallback);
        }else{
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            loaderManager.restartLoader(LOAD_PATH, bundle, loaderCallback);
        }
    }

    /** 图片加载器回调 */
    private LoaderManager.LoaderCallbacks<Cursor> loaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader cursor = null;
            if(id == LOAD_ALL){
                cursor = new CursorLoader(CONTEXT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,//内容提供者的Uri
                        IMAGE_PROJECTION,
                        IMAGE_PROJECTION[4]+">0 AND "+IMAGE_PROJECTION[3]+"=? OR "+IMAGE_PROJECTION[3]+"=? ",
                        new String[]{"image/jpeg", "image/png"},
                        IMAGE_PROJECTION[2] + " DESC");
            }else if(id == LOAD_PATH){
                cursor = new CursorLoader(CONTEXT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_PROJECTION,
                        IMAGE_PROJECTION[4]+">0 AND "+IMAGE_PROJECTION[0]+" like '%"+args.getString("path")+"%'",
                        null,
                        IMAGE_PROJECTION[2] + " DESC");
            }
            return cursor;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(data == null || data.getCount() <= 0 || data.getCount()-1 == datas.size()){
                return;
            }
            datas.clear();
            data.moveToFirst();
            while (data.moveToNext()){
                String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                if(!FileUtils.exists(path)){
                    continue;//中断该循环进行下一个循环
                }

                String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                PictureBean picture = null;
                if(name != null && !TextUtils.isEmpty(name.trim())){
                    picture = new PictureBean();
                    picture.imagePath = path;
                    picture.imageName = name;
                    picture.imageDate = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                    picture.imageSize = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
                    datas.add(picture);
                }

                if(isFirstInit){
                    File folderFile = new File(path).getParentFile();
                    if(FileUtils.exists(folderFile)){
                        String fp = folderFile.getAbsolutePath();
                        FolderBean f = getFolderByPath(fp);
                        if(f == null){
                            FolderBean folder = new FolderBean();
                            folder.folderName = folderFile.getName();
                            folder.folderPath = fp;
                            folder.picture = picture;
                            folder.pics = new ArrayList<>();
                            folder.pics.add(picture);
                            folders.add(folder);
                        }else {
                            f.pics.add(picture);
                        }
                    }
                }
            }
            //全部图片
            if(isFirstInit && !folders.isEmpty()){
                FolderBean allFolder = new FolderBean();
                allFolder.folderName = "全部图片";
                allFolder.folderPath = null;
                allFolder.pics = null;
                allFolder.picture = folders.get(0).picture;
                folders.add(0, allFolder);
            }
            isFirstInit = false;//已经初始化完成
            //通知View刷新了数据
            view.notifyDataSetChanged(datas, folders);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            //doNothing
        }
    };

    /** 根据路径从 folders集合 中返回已有的folder，若没有则返回null */
    private FolderBean getFolderByPath(@NonNull String folderPath) {
        if(!folders.isEmpty()){
            for(FolderBean bean:folders){
                if(folderPath.equals(bean.folderPath)){
                    return bean;
                }
            }
        }
        return null;
    }

    @Override
    public void onDetach() {
        datas.clear();
        folders.clear();
        super.onDetach();
    }
}

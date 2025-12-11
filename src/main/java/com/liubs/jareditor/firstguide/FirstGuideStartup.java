package com.liubs.jareditor.firstguide;

import com.intellij.openapi.application.ApplicationManager;
import com.liubs.jareditor.persistent.GuideStorage;
import com.liubs.jareditor.util.ClientVersions;

/**
 * @author Liubsyy
 * @date 2025/12/9
 */
public class FirstGuideStartup {
    private static volatile boolean alreadyCreated = false;

    public static void showGuide(){
        //只创建一次，所有窗口都共用一个
        if(!alreadyCreated) {
            synchronized (FirstGuideStartup.class) {
                if(!alreadyCreated) {
                    alreadyCreated = true;
                    run();
                }
            }
        }
    }

    public static void run() {

        GuideStorage.getInstance().setVersion(ClientVersions.getCurrentPluginVersion());

        if(GuideStorage.getInstance().isShowed() || GuideStorage.getInstance().getCount() >=3){
            return;
        }
        GuideStorage.getInstance().setCount(GuideStorage.getInstance().getCount()+1);

        ApplicationManager.getApplication().invokeLater(() -> {
            try{
                FirstGuideDialog dialog = new FirstGuideDialog();
                if(dialog.showAndGet()){
                    GuideStorage.getInstance().setShowed(true);
                }
            }catch (Throwable e) {
                e.printStackTrace();
            }
        });

    }
}

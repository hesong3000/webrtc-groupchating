package com.ichat.utility;

import android.os.Handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gaoqi on 2016/8/2.
 */
public class HandlerHolder {
    private Map<Integer,Handler> handlers = new HashMap<Integer,Handler>();

    public boolean registHandler(Integer key, Handler handler){
        synchronized (handlers){
            handlers.put(key,handler);
        }
        return true;
    }
    public Handler unregistHandler(Integer key){
        Handler handler = null;
        synchronized(handlers){
            handler = handlers.remove(key);
        }
        return handler;
    }

    public int sendMsgToAll(ISendAction action){
        int ret = 0;
        synchronized (handlers){
            Set<Map.Entry<Integer,Handler>> set = handlers.entrySet();
            ret = set.size();
            for(Map.Entry<Integer,Handler> e : set){
                action.SendAction(e.getKey(),e.getValue());
            }
        }
        return ret;
    }

    public interface ISendAction{
        public boolean SendAction(Integer key, Handler handler);
    }
}
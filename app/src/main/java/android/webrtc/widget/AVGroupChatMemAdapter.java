package android.webrtc.widget;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Jaeger on 16/2/24.
 *
 * Email: chjie.jaeger@gamil.com
 * GitHub: https://github.com/laobie
 */
public abstract class AVGroupChatMemAdapter<T> {
    protected ImageView generateView(Context context){
        ImageView layout = new ImageView(context);
        return layout;
    }
}

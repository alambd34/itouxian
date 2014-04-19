package volley.toolbox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import volley.NetworkResponse;
import volley.Request;
import volley.Response;

/**
 * Created by chenjishi on 14-3-15.
 */
public class GsonRequest<T> extends Request<T> {
    private final Gson mGson;
    private final Class<T> mClazz;
    private final Response.Listener<T> mListener;

    public GsonRequest(int method,
                       String url,
                       Class<T> clazz,
                       Response.Listener<T> listener,
                       Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mClazz = clazz;
        mListener = listener;
        mGson = new GsonBuilder().create();
    }

    public GsonRequest(String url,
                       Class<T> clazz,
                       Response.Listener<T> listener,
                       Response.ErrorListener errorListener) {
        this(Method.GET, url, clazz, listener, errorListener);
    }

    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        final String json = new String(response.data);

        return Response.success(mGson.fromJson(json, mClazz),
                HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }
}

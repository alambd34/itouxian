package volley.toolbox;

import volley.NetworkResponse;
import volley.Request;
import volley.Response;

/**
 * Created by chenjishi on 14-4-2.
 */
public class ByteArrayRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> mListener;

    public ByteArrayRequest(int method, String url, Response.Listener<byte[]> listener,
                            Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    public ByteArrayRequest(String url, Response.Listener<byte[]> listener,
                            Response.ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
    }
}

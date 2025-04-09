package com.fourt.RailSkylines.util;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.servlet.http.HttpServletResponse;
import com.fourt.RailSkylines.domain.RestResponse;

@ControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {

    @Override
    @Nullable
    public Object beforeBodyWrite(@Nullable Object arg0, MethodParameter arg1, MediaType arg2, Class arg3,
            ServerHttpRequest arg4, ServerHttpResponse arg5) {
                HttpServletResponse servletResponse = ((ServletServerHttpResponse) arg5).getServletResponse();
                int status = servletResponse.getStatus();
                RestResponse<Object> res = new RestResponse<Object>();
                res.setStatusCode(status);
                if(arg0 instanceof String)
                {
                    return arg0;
                }
                if(arg0 instanceof String)
                {
                    return arg0;
                }
                if(status >400){
                //case error
                // res.setError(arg0.toString());
                // res.setMessage("Call API error!");
                return arg0;
                }else {
                //case success  
                res.setData(arg0);
                res.setMessage("Call API success!");
                }
                return res;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

}

package com.rpc;

import com.rpc.annotation.Service;
import com.rpc.api.ByeService;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-03-01 00:51
 * @description:
 **/
@Service
public class ByeServiceImpl implements ByeService {
    @Override
    public String bye(String name) {
        return "bye, " + name;
    }
}

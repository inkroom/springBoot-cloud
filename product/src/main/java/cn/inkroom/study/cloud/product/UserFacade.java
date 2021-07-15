package cn.inkroom.study.cloud.product;

/**
 * @author inkbox
 * @date 2021/7/14
 */
public interface UserFacade {
    String timeout();

    String exception();

    String getUser(Long id);

    String updateUser(Long id);
}

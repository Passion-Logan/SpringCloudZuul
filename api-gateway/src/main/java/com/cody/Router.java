package com.cody;

/**
 * 服务网关（路由配置）<p>
 * 代码描述<p>
 * Copyright: Copyright (C) 2019 XXX, Inc. All rights reserved. <p>
 *
 * @author WQL
 * @since 2019年12月17日 0017 19:07
 */
public class Router {
    /**
     *传统路由配置
     * 所谓的传统路由配置方式就是在不依赖于服务发现机制的情况下，通过在配置文件中具体指定每个路由表达式与服务实例的映射关系来实现API网关对外部请求的路由。
     *
     * 没有Eureka和Consul的服务治理框架帮助的时候，我们需要根据服务实例的数量采用不同方式的配置来实现路由规则：
     *
     * 单实例配置：通过一组zuul.routes.<route>.path与zuul.routes.<route>.url参数对的方式配置，比如：
     * zuul.routes.user-service.path=/user-service/**
     * zuul.routes.user-service.url=http://localhost:8080/
     * 该配置实现了对符合/user-service/**规则的请求路径转发到http://localhost:8080/地址的路由规则，比如，当有一个请求http://localhost:1101/user-service/hello被发送到API网关上，
     * 由于/user-service/hello能够被上述配置的path规则匹配，所以API网关会转发请求到http://localhost:8080/hello地址。
     *
     * 多实例配置：通过一组zuul.routes.<route>.path与zuul.routes.<route>.serviceId参数对的方式配置，比如：
     * zuul.routes.user-service.path=/user-service/**
     * zuul.routes.user-service.serviceId=user-service
     *
     * ribbon.eureka.enabled=false
     * user-service.ribbon.listOfServers=http://localhost:8080/,http://localhost:8081/
     * 该配置实现了对符合/user-service/**规则的请求路径转发到http://localhost:8080/和http://localhost:8081/两个实例地址的路由规则。它的配置方式与服务路由的配置方式一样，
     * 都采用了zuul.routes.<route>.path与zuul.routes.<route>.serviceId参数对的映射方式，只是这里的serviceId是由用户手工命名的服务名称，
     * 配合<serviceId>.ribbon.listOfServers参数实现服务与实例的维护。由于存在多个实例，API网关在进行路由转发时需要实现负载均衡策略，
     * 于是这里还需要Spring Cloud Ribbon的配合。由于在Spring Cloud Zuul中自带了对Ribbon的依赖，所以我们只需要做一些配置即可，比如上面示例中关于Ribbon的各个配置，它们的具体作用如下：
     *
     * ribbon.eureka.enabled：由于zuul.routes.<route>.serviceId指定的是服务名称，默认情况下Ribbon会根据服务发现机制来获取配置服务名对应的实例清单。
     * 但是，该示例并没有整合类似Eureka之类的服务治理框架，所以需要将该参数设置为false，不然配置的serviceId是获取不到对应实例清单的。
     * user-service.ribbon.listOfServers：该参数内容与zuul.routes.<route>.serviceId的配置相对应，开头的user-service对应了serviceId的值，
     * 这两个参数的配置相当于在该应用内部手工维护了服务与实例的对应关系。
     * 不论是单实例还是多实例的配置方式，我们都需要为每一对映射关系指定一个名称，也就是上面配置中的<route>，每一个<route>就对应了一条路由规则。
     * 每条路由规则都需要通过path属性来定义一个用来匹配客户端请求的路径表达式，并通过url或serviceId属性来指定请求表达式映射具体实例地址或服务名。
     *
     * 服务路由配置
     * 服务路由我们在上一篇中也已经有过基础的介绍和体验，Spring Cloud Zuul通过与Spring Cloud Eureka的整合，实现了对服务实例的自动化维护，
     * 所以在使用服务路由配置的时候，我们不需要向传统路由配置方式那样为serviceId去指定具体的服务实例地址，只需要通过一组zuul.routes.<route>.path与zuul.routes.<route>.serviceId参数对的方式配置即可。
     *
     * 比如下面的示例，它实现了对符合/user-service/**规则的请求路径转发到名为user-service的服务实例上去的路由规则。其中<route>可以指定为任意的路由名称。
     *
     * zuul.routes.user-service.path=/user-service/**
     * zuul.routes.user-service.serviceId=user-service
     * 对于面向服务的路由配置，除了使用path与serviceId映射的配置方式之外，还有一种更简洁的配置方式：zuul.routes.<serviceId>=<path>，其
     * 中<serviceId>用来指定路由的具体服务名，<path>用来配置匹配的请求表达式。比如下面的例子，它的路由规则等价于上面通过path与serviceId组合使用的配置方式。
     *
     * zuul.routes.user-service=/user-service/**
     * 传统路由的映射方式比较直观且容易理解，API网关直接根据请求的URL路径找到最匹配的path表达式，直接转发给该表达式对应的url或对应serviceId下
     * 配置的实例地址，以实现外部请求的路由。那么当采用path与serviceId以服务路由方式实现时候，没有配置任何实例地址的情况下，外部
     * 请求经过API网关的时候，它是如何被解析并转发到服务具体实例的呢？
     *
     * 在Spring Cloud Netflix中，Zuul巧妙的整合了Eureka来实现面向服务的路由。实际上，我们可以直接将API网关也看做是Eureka服
     * 务治理下的一个普通微服务应用。它除了会将自己注册到Eureka服务注册中心上之外，也会从注册中心获取所有服务以及它们的实例清单。所以，在Eureka的
     * 帮助下，API网关服务本身就已经维护了系统中所有serviceId与实例地址的映射关系。当有外部请求到达API网关的时候，根据请求的URL路径找到
     * 最佳匹配的path规则，API网关就可以知道要将该请求路由到哪个具体的serviceId上去。由于在API网关中已经知道serviceId对应服务实例的地址清单，
     * 那么只需要通过Ribbon的负载均衡策略，直接在这些清单中选择一个具体的实例进行转发就能完成路由工作了。
     */


    /**
     * 在Spring Cloud Zuul中实现的过滤器必须包含4个基本特征：过滤类型、执行顺序、执行条件、具体操作。
     * 这些元素看着似乎非常的熟悉，实际上它就是ZuulFilter接口中定义的四个抽象方法：
     * String filterType();
     *
     * int filterOrder();
     *
     * boolean shouldFilter();
     *
     * Object run();
     *
     *
     * filterType：该函数需要返回一个字符串来代表过滤器的类型，而这个类型就是在HTTP请求过程中定义的各个阶段。
     * 在Zuul中默认定义了四种不同生命周期的过滤器类型，具体如下：
     *     pre：可以在请求被路由之前调用。
     *     routing：在路由请求时候被调用。
     *     post：在routing和error过滤器之后被调用。
     *     error：处理请求时发生错误时被调用。
     * filterOrder：通过int值来定义过滤器的执行顺序，数值越小优先级越高。
     * shouldFilter：返回一个boolean类型来判断该过滤器是否要执行。我们可以通过此方法来指定过滤器的有效范围。
     * run：过滤器的具体逻辑。在该函数中，我们可以实现自定义的过滤逻辑，来确定是否要拦截当前的请求，不对其进行后续的路由，
     * 或是在请求路由返回结果之后，对处理结果做一些加工等。
     */
}

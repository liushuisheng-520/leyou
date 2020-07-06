package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.entity.Goods;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 将传过来spu转换成Goods,并返回到ElasticSearchInitTest中
 */
@Service
public class SearchService {
    @Autowired
    private ItemClient itemClient;

    public Goods buildGoods(SpuDTO spuDTO) {
        //给Goods中的属性赋值
        Goods goods = new Goods();

        goods.setId(spuDTO.getId());
        goods.setBrandId(spuDTO.getBrandId());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setCreateTime(spuDTO.getCreateTime().getTime());
        String all = spuDTO.getName() + spuDTO.getBrandName() + spuDTO.getCategoryName();
        goods.setAll(all);
        goods.setSubTitle(spuDTO.getSubTitle());

        //通过feign接口获取SKU数据
        List<SkuDTO> skuDTOList = itemClient.findSKUBySpuId(spuDTO.getId());

        //创建积集合存入map
        List<Map> skus = new ArrayList<>();

        //遍历skuDTOList获取每个sku
        for (SkuDTO skuDTO : skuDTOList) {
            //创建Map将SKU的数据以键值对的形式存储
            Map map = new HashMap<>();
            map.put("id", skuDTO.getId());
            map.put("title", skuDTO.getTitle());
            map.put("price", skuDTO.getPrice());
            map.put("image", StringUtils.substringBefore(skuDTO.getImages(), ", "));

            skus.add(map);
        }
        //[id:,title:,price:,image:]

        //给Skus属性赋值
        goods.setSkus(JsonUtils.toString(skus));

        //给Price属性赋值
        //用流式编程收集每个skuDTOList的价格放入到一个set集合中
        Set<Long> priceSet = skuDTOList.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());
        goods.setPrice(priceSet);


        //根据spuId查询spudetail
        SpuDetailDTO spuDetail = itemClient.findSPUDetailBySpuId(spuDTO.getId());

        //获取spudetail中的通用规格数据,并将其转换成Map格式(将其中的对应的规格参数id改成对应的名称)
        String genericSpec = spuDetail.getGenericSpec();
        Map<Long, String> genericSpecMap = JsonUtils.toMap(genericSpec, Long.class, String.class);

        //获取spudetail中的特殊规格数据,并将其转换成Map格式(将其中的对应的规格参数id改成对应的名称)
        String specialSpec = spuDetail.getSpecialSpec();
        Map<Long, List<String>> specialSpecMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<String>>>() {
        });

        //创建一个Map,用于存放spudetail表中generic字段中specParamId对应的规格名称和价格区间
        Map<String, Object> specsMap = new HashMap<>();
        //根据分类Id查询specParam中的规格参数
        List<SpecParamDTO> specParamList = itemClient.findSpecParamByCategoryIdOrGroupId(null, spuDTO.getCid3(), true);//只会把用来做搜索的规格参数
        //遍历集合 获取每个规格参数
        for (SpecParamDTO specParam : specParamList) {
            //获取的规格参数id对应的规格名称
            String key = specParam.getName();
            Object value = "";

            //判断specParam中的Generic字段中的数据和spuDetail的generic字段值是否相同
            if (specParam.getGeneric()) {
                value = genericSpecMap.get(specParam.getId());
            } else {
                //也判断specParam中的Generic字段中的数据和spuDetail的special字段值是否相同
                value = specialSpecMap.get(specParam.getId());
            }
            //如果以上都不是 那么就判断是不是数值型 如果是数值型,需要凑成区间查询  0-1,0-1.5,0-2,0-2.5,0-3,
            if (specParam.getIsNumeric()) {
                //调用chooseSegment方法生成价格区间
                value = chooseSegment(value, specParam);
            }

            //将spudetail表中generic字段中specParamId对应的规格名称和价格区间存入到一个Map中
            specsMap.put(key, value);
        }


        //将Map设置到Specs(价格区间)属性中
        goods.setSpecs(specsMap);

        return goods;


    }

    //形成价格区间
    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }


    @Autowired
    private ElasticsearchTemplate esTemplate;


    /**
     * 根据关键字分页查询商品
     *
     * @param searchRequest
     * @return
     */
    public PageResult<GoodsDTO> findGoodsByPage(SearchRequest searchRequest) {
        //获取关键字
        String key = searchRequest.getKey();
        //判断关键字是否为空
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        //GET /leyou/_search
        //{
        //"_source": {
        //  "includes":["id","subTitle","skus"]
        //},
        //"query": {
        //  "match": {
        //    "all": "手机"
        //  }
        //},
        //      "from": 0,
        //     "size": 20
        //}

        //根据关键字查询 查询结果只要id,subTitle,skus
        //ES和Spring整合的SpringDataElasticSearch
        //SpringDataElasticSearch结合ES原生的查询方式

        //用来构建查询方式的(创建要显示的字段)
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //设置显示字段的过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        //根据关键字走match查询
        buildBasicQuery(searchRequest, queryBuilder);
        //设置分页 起始位置和每页显示的条数
        //其实位置的计算公式(当前页数1)*每页显示的条数
        //获取当前页数和每页显示的条数
        Integer page = searchRequest.getPage();
        Integer size = searchRequest.getSize();

        //封装了 from和size
        queryBuilder.withPageable(PageRequest.of(page - 1, size));

        //分页查询结果 用Goods接收
        AggregatedPage<Goods> aggregatedPage = esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        //当前页的数据
        List<Goods> goodsList = aggregatedPage.getContent();
        //判断goodsList是否为空
        if (CollectionUtils.isEmpty(goodsList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //转换成GoodsDTO格式
        List<GoodsDTO> goodsDTOSList = BeanHelper.copyWithCollection(goodsList, GoodsDTO.class);

        //总条数
        long total = aggregatedPage.getTotalElements();
        //总页数
        Integer totalPages = aggregatedPage.getTotalPages();


        return new PageResult<GoodsDTO>(total, totalPages.longValue(), goodsDTOSList);
    }



    //抽取查询关键字
    public void buildBasicQuery(SearchRequest searchRequest, NativeSearchQueryBuilder queryBuilder) {
        //根据筛选条件进行查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //构造关键字和过滤条件
        boolQuery.must(QueryBuilders.matchQuery("all", searchRequest.getKey()));
        //获取前端传的filterMap集合
        Map<String, Object> filterMap = searchRequest.getFilterMap();
        //判断是否为空
        if (filterMap != null) {
            //遍历Map
            for (String key : filterMap.keySet()) {
                //判断传过来的key是不是品牌 是就把查询条件修改成brandId
                if (key.equals("品牌")) {
                    boolQuery.filter(QueryBuilders.termsQuery("brandId", filterMap.get(key)));
                    //判断传过来的key是不是分类 是就把查询条件修改成categoryId
                } else if (key.equals("分类")) {
                    boolQuery.filter(QueryBuilders.termsQuery("categoryId", filterMap.get(key)));

                } else {//查询所有规格参数
                    boolQuery.filter(QueryBuilders.termsQuery("specs." + key, filterMap.get(key)));
                }
            }
        }


        queryBuilder.withQuery(boolQuery);

    }

    /**
     * 根据品牌Id和分类Id过滤查询
     *
     * @param searchRequest
     * @return
     */
    public Map<String, List<?>> filter(SearchRequest searchRequest) {
        //用于存储最后结果的
        Map<String, List<?>> resultMap = new HashMap<String, List<?>>();

        //获取关键字
        String key = searchRequest.getKey();
        //判断关键字是否为空
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        //GET /leyou/_search
        //{
        //"_source": {
        //  "includes":["id","subTitle","skus"]
        //},
        //"query": {
        //  "match": {
        //    "all": "手机"
        //  }
        //
        //},
        //"from": 0,
        //"size": 0,
        // "aggs": {
        //  "brandAgg": {
        //    "terms": {
        //      "field": "brandId",
        //      "size": 20
        //    }
        //  }
        //}
        //}

        //用来构建查询方式的(创建要显示的字段)
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //设置显示字段的过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(null, null));
        //根据关键字走match查询
        buildBasicQuery(searchRequest, queryBuilder);
        //设置分页 起始位置和每页显示的条数
        //其实位置的计算公式(当前页数1)*每页显示的条数
        //获取当前页数和每页显示的条数
        Integer page = searchRequest.getPage();

        //封装了 from和size
        queryBuilder.withPageable(PageRequest.of(page - 1, 1));

//-------------------------------------品牌聚合------------------------------------------------
        //设置品牌聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("brandAgg").field("brandId").size(20));
        //设置分类聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("categoryAgg").field("categoryId").size(20));

        //分页查询 返回结果
        AggregatedPage<Goods> aggregatedPage = esTemplate.queryForPage(queryBuilder.build(), Goods.class);


        //通过结果获取Aggregations
        Aggregations aggregations = aggregatedPage.getAggregations();

        //再通过Aggregation和起的名字获取到terms(Terms是aggregation的子接口)
        Terms termsBrand = aggregations.get("brandAgg");
        //再通过terms获取Buckets
        List<? extends Terms.Bucket> bucketsBrand = termsBrand.getBuckets();
        //通过流式编程获取到品牌的Id(brandId)
        List<Long> brandId = bucketsBrand.stream().map(Terms.Bucket::getKeyAsNumber).map(Number::longValue).collect(Collectors.toList());
        //还需要通过品牌Id集合查询品牌名称集合 通过feign的方式调用itemClient的接口
        List<BrandDTO> brandList = itemClient.findBrandByIds(brandId);
        resultMap.put("品牌", brandList);

//-------------------------------------分类聚合--------------------------------------------------
        //再通过Aggregation和起的名字获取到terms(Terms是aggregation的子接口)
        Terms termscategory = aggregations.get("categoryAgg");
        //再通过terms获取Buckets
        List<? extends Terms.Bucket> bucketscategory = termscategory.getBuckets();
        //通过流式编程获取到分类的Id(brandId)
        List<Long> categoryIdList = bucketscategory.stream().map(Terms.Bucket::getKeyAsNumber).map(Number::longValue).collect(Collectors.toList());

        //还需要通过分类Id集合查询分类名称集合 通过feign的方式调用itemClient的接口
        List<CategoryDTO> categoryList = itemClient.findCategoryByIds(categoryIdList);

        resultMap.put("分类", categoryList);

//------------------------------------根据分类Id获取规格参数并进行规格参数聚合----------------------------------------------------
        //判断分类Id是否为空
        if (categoryIdList != null && categoryIdList.size() > 0) {
            //获取第一个Id
            Long categoryId = categoryIdList.get(0);
            //根据分类Id查询规格参数
            List<SpecParamDTO> paramList = itemClient.findSpecParamByCategoryIdOrGroupId(null, categoryId, true);
            //创建NativeSearchQueryBuilder用于存放聚合条件
            NativeSearchQueryBuilder queryParamBuilder = new NativeSearchQueryBuilder();
            //不显示Goods字段属性
            queryParamBuilder.withSourceFilter(new FetchSourceFilter(null, null));

            //关键字查询
            buildBasicQuery(searchRequest, queryParamBuilder);
            //配置页数
            queryParamBuilder.withPageable(PageRequest.of(0, 1));

            //循环遍历规格参数集合 获取到每个参数
            for (SpecParamDTO param : paramList) {
                //获取规格参数名称
                String paramName = param.getName();
                //构造聚合条件
                queryParamBuilder.addAggregation(AggregationBuilders.terms(paramName + "Agg").field("specs." + paramName).size(20));
            }
            //执行查询ES中的规格参数数据
            AggregatedPage<Goods> aggregatedAggPage = esTemplate.queryForPage(queryParamBuilder.build(), Goods.class);

            //获取ES中的Aggregations字段
            Aggregations pageAggregations = aggregatedAggPage.getAggregations();
            //循环遍历规格参数集合
            for (SpecParamDTO param : paramList) {
                //获取规格名称
                String paramName = param.getName();
                //根据Aggregations字段获取聚合名称(用子接口)
                Terms terms = pageAggregations.get(paramName + "Agg");
                //根据聚合名称获取Bucketsz字段
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                //用流式编程获取Buckets中的Key值
                List<String> specParamList = buckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                //存入Map中
                resultMap.put(paramName, specParamList);
            }
        }


        return resultMap;
    }

    @Autowired
    private GoodsRepository goodsRepository;

    /**
     * 添加上架索引
     * @param spuId
     */
    public void createGoods(Long spuId) {
        SpuDTO spu = itemClient.findSpuBySpuId(spuId);
        //此时brandName和categoryName是空的
        //获取brandName
        BrandDTO brandById = itemClient.findBrandById(spu.getBrandId());
        spu.setBrandName(brandById.getName());

        //获取categoryName
        List<CategoryDTO> categoryList = itemClient.findCategoryByIds(spu.getCategoryIds());
        String categoryNames = categoryList.stream().map(CategoryDTO::getName).collect(Collectors.joining("/"));
        spu.setCategoryName(categoryNames);


        Goods goods = this.buildGoods(spu);
        goodsRepository.save(goods);

        System.out.println("索引文件创建成功");

    }

    /**
     * 删除下架索引
     * @param spuId
     */
    public void removeGoods(Long spuId) {
        goodsRepository.deleteById(spuId);

        System.out.println("索引文件删除成功");
    }
}

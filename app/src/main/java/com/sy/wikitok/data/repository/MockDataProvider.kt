package com.sy.wikitok.data.repository

import com.sy.wikitok.data.model.WikiApiResponse
import com.sy.wikitok.data.model.WikiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * @author Yeung
 * @date 2025/3/20
 */
class MockDataProvider {

    companion object {
        private const val url1 =
            "https://gips3.baidu.com/it/u=1821127123,1149655687&fm=3028&app=3028&f=JPEG&fmt=auto?w=720&h=1280"
        private const val url2 =
            "https://gips1.baidu.com/it/u=3874647369,3220417986&fm=3028&app=3028&f=JPEG&fmt=auto?w=720&h=1280"
    }

    suspend fun mockListResult(): Result<List<WikiModel>> {
        return runCatching() {
            withContext(Dispatchers.IO) {
                delay(3_000L)
                mockData().query.pages.filter {
                    it.value.thumbnail != null
                }.map {
                    WikiModel(
                        id = it.value.pageid.toString(),
                        title = it.value.title,
                        content = it.value.extract,
//                        coverUrl = it.value.thumbnail!!.source,
                        imgUrl = if (it.value.pageid % 2 == 0) url1 else url2,
                        linkUrl = it.value.canonicalurl
                    )
                }
            }
        }
    }

    fun mockListData(): List<WikiModel> {
        return mockData().query.pages.filter {
            it.value.thumbnail != null
        }.map {
            WikiModel(
                id = it.value.pageid.toString(),
                title = it.value.title,
                content = it.value.extract,
//                        coverUrl = it.value.thumbnail!!.source,
                imgUrl = if (it.value.pageid % 2 == 0) url1 else url2,
                linkUrl = it.value.canonicalurl
            )
        }
    }


    fun mockData(): WikiApiResponse {
        val jsonString = "{\n" +
                "    \"batchcomplete\": \"\",\n" +
                "    \"continue\": {\n" +
                "        \"grncontinue\": \"0.804476740442|0.804495622518|6495800|0\",\n" +
                "        \"continue\": \"grncontinue||\"\n" +
                "    },\n" +
                "    \"query\": {\n" +
                "        \"pages\": {\n" +
                "            \"3084274\": {\n" +
                "                \"pageid\": 3084274,\n" +
                "                \"ns\": 0,\n" +
                "                \"title\": \"好莱坞 (阿拉巴马州)\",\n" +
                "                \"extract\": \"好莱坞（英文：Hollywood），是美国阿拉巴马州下属的一座城市。面积约为8.92平方英里（约合23.09平方公里）。根据2010年美国人口普查，该市有人口1000人，人口密度为112.16/平方英里（约合43.31/平方公里）。\",\n" +
                "                \"contentmodel\": \"wikitext\",\n" +
                "                \"pagelanguage\": \"zh\",\n" +
                "                \"pagelanguagehtmlcode\": \"zh\",\n" +
                "                \"pagelanguagedir\": \"ltr\",\n" +
                "                \"touched\": \"2025-03-14T16:12:38Z\",\n" +
                "                \"lastrevid\": 62732885,\n" +
                "                \"length\": 1480,\n" +
                "                \"fullurl\": \"https://zh.wikipedia.org/wiki/%E5%A5%BD%E8%8E%B1%E5%9D%9E_(%E9%98%BF%E6%8B%89%E5%B7%B4%E9%A9%AC%E5%B7%9E)\",\n" +
                "                \"editurl\": \"https://zh.wikipedia.org/w/index.php?title=%E5%A5%BD%E8%8E%B1%E5%9D%9E_(%E9%98%BF%E6%8B%89%E5%B7%B4%E9%A9%AC%E5%B7%9E)&action=edit\",\n" +
                "                \"canonicalurl\": \"https://zh.wikipedia.org/wiki/%E5%A5%BD%E8%8E%B1%E5%9D%9E_(%E9%98%BF%E6%8B%89%E5%B7%B4%E9%A9%AC%E5%B7%9E)\",\n" +
                "                \"varianttitles\": {\n" +
                "                    \"zh\": \"好莱坞 (阿拉巴马州)\",\n" +
                "                    \"zh-hans\": \"好莱坞 (阿拉巴马州)\",\n" +
                "                    \"zh-hant\": \"好萊塢 (阿拉巴馬州)\",\n" +
                "                    \"zh-cn\": \"好莱坞 (阿拉巴马州)\",\n" +
                "                    \"zh-hk\": \"荷里活 (阿拉巴馬州)\",\n" +
                "                    \"zh-mo\": \"荷里活 (阿拉巴馬州)\",\n" +
                "                    \"zh-my\": \"好莱坞 (阿拉巴马州)\",\n" +
                "                    \"zh-sg\": \"好莱坞 (阿拉巴马州)\",\n" +
                "                    \"zh-tw\": \"好萊塢 (阿拉巴馬州)\"\n" +
                "                },\n" +
                "                \"thumbnail\": {\n" +
                "                    \"source\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Jackson_County_Alabama_Incorporated_and_Unincorporated_areas_Hollywood_Highlighted.svg/800px-Jackson_County_Alabama_Incorporated_and_Unincorporated_areas_Hollywood_Highlighted.svg.png\",\n" +
                "                    \"width\": 800,\n" +
                "                    \"height\": 471\n" +
                "                }\n" +
                "            },\n" +
                "            \"3885139\": {\n" +
                "                \"pageid\": 3885139,\n" +
                "                \"ns\": 0,\n" +
                "                \"title\": \"萬順路\",\n" +
                "                \"extract\": \"万顺路（Wanshun Rd.）是屏东县万丹乡的东西向主要干道。为台88线下之平面侧车道，全线属市道188号。本道路共分成二段，连结万丹市郊。西起于仑顶路口接万大大桥往高雄市大寮、凤山区。途中经过万丹交流道并与属台27线的中兴路一、二段交叉，可转往万丹市区、屏东市、新园乡及东港镇。\",\n" +
                "                \"contentmodel\": \"wikitext\",\n" +
                "                \"pagelanguage\": \"zh\",\n" +
                "                \"pagelanguagehtmlcode\": \"zh\",\n" +
                "                \"pagelanguagedir\": \"ltr\",\n" +
                "                \"touched\": \"2025-03-14T16:14:46Z\",\n" +
                "                \"lastrevid\": 70095219,\n" +
                "                \"length\": 4486,\n" +
                "                \"fullurl\": \"https://zh.wikipedia.org/wiki/%E8%90%AC%E9%A0%86%E8%B7%AF\",\n" +
                "                \"editurl\": \"https://zh.wikipedia.org/w/index.php?title=%E8%90%AC%E9%A0%86%E8%B7%AF&action=edit\",\n" +
                "                \"canonicalurl\": \"https://zh.wikipedia.org/wiki/%E8%90%AC%E9%A0%86%E8%B7%AF\",\n" +
                "                \"varianttitles\": {\n" +
                "                    \"zh\": \"萬順路\",\n" +
                "                    \"zh-hans\": \"万顺路\",\n" +
                "                    \"zh-hant\": \"萬順路\",\n" +
                "                    \"zh-cn\": \"万顺路\",\n" +
                "                    \"zh-hk\": \"萬順路\",\n" +
                "                    \"zh-mo\": \"萬順路\",\n" +
                "                    \"zh-my\": \"万顺路\",\n" +
                "                    \"zh-sg\": \"万顺路\",\n" +
                "                    \"zh-tw\": \"萬順路\"\n" +
                "                },\n" +
                "                \"thumbnail\": {\n" +
                "                    \"source\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Jackson_County_Alabama_Incorporated_and_Unincorporated_areas_Hollywood_Highlighted.svg/800px-Jackson_County_Alabama_Incorporated_and_Unincorporated_areas_Hollywood_Highlighted.svg.png\",\n" +
                "                    \"width\": 800,\n" +
                "                    \"height\": 471\n" +
                "                }\n" +
                "            },\n" +
                "            \"3310944\": {\n" +
                "                \"pageid\": 3310944,\n" +
                "                \"ns\": 0,\n" +
                "                \"title\": \"見風紅\",\n" +
                "                \"extract\": \"见风红（学名：Lindernia pusilla），又名细茎母草，为母草属下的一种一年生草本植物，叶卵状披针形，边缘有锯齿，两面均披柔毛。花白色或浅蓝色，腋生或顶生。结蒴果。\",\n" +
                "                \"contentmodel\": \"wikitext\",\n" +
                "                \"pagelanguage\": \"zh\",\n" +
                "                \"pagelanguagehtmlcode\": \"zh\",\n" +
                "                \"pagelanguagedir\": \"ltr\",\n" +
                "                \"touched\": \"2025-03-14T16:13:52Z\",\n" +
                "                \"lastrevid\": 80012471,\n" +
                "                \"length\": 767,\n" +
                "                \"fullurl\": \"https://zh.wikipedia.org/wiki/%E8%A6%8B%E9%A2%A8%E7%B4%85\",\n" +
                "                \"editurl\": \"https://zh.wikipedia.org/w/index.php?title=%E8%A6%8B%E9%A2%A8%E7%B4%85&action=edit\",\n" +
                "                \"canonicalurl\": \"https://zh.wikipedia.org/wiki/%E8%A6%8B%E9%A2%A8%E7%B4%85\",\n" +
                "                \"varianttitles\": {\n" +
                "                    \"zh\": \"見風紅\",\n" +
                "                    \"zh-hans\": \"见风红\",\n" +
                "                    \"zh-hant\": \"見風紅\",\n" +
                "                    \"zh-cn\": \"见风红\",\n" +
                "                    \"zh-hk\": \"見風紅\",\n" +
                "                    \"zh-mo\": \"見風紅\",\n" +
                "                    \"zh-my\": \"见风红\",\n" +
                "                    \"zh-sg\": \"见风红\",\n" +
                "                    \"zh-tw\": \"見風紅\"\n" +
                "                },\n" +
                "                \"thumbnail\": {\n" +
                "                    \"source\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Jackson_County_Alabama_Incorporated_and_Unincorporated_areas_Hollywood_Highlighted.svg/800px-Jackson_County_Alabama_Incorporated_and_Unincorporated_areas_Hollywood_Highlighted.svg.png\",\n" +
                "                    \"width\": 800,\n" +
                "                    \"height\": 471\n" +
                "                }\n" +
                "            },\n" +
                "            \"13199\": {\n" +
                "                \"pageid\": 13199,\n" +
                "                \"ns\": 0,\n" +
                "                \"title\": \"314年\",\n" +
                "                \"extract\": \"\",\n" +
                "                \"contentmodel\": \"wikitext\",\n" +
                "                \"pagelanguage\": \"zh\",\n" +
                "                \"pagelanguagehtmlcode\": \"zh\",\n" +
                "                \"pagelanguagedir\": \"ltr\",\n" +
                "                \"touched\": \"2024-12-26T13:42:15Z\",\n" +
                "                \"lastrevid\": 74470361,\n" +
                "                \"length\": 1057,\n" +
                "                \"fullurl\": \"https://zh.wikipedia.org/wiki/314%E5%B9%B4\",\n" +
                "                \"editurl\": \"https://zh.wikipedia.org/w/index.php?title=314%E5%B9%B4&action=edit\",\n" +
                "                \"canonicalurl\": \"https://zh.wikipedia.org/wiki/314%E5%B9%B4\",\n" +
                "                \"varianttitles\": {\n" +
                "                    \"zh\": \"314年\",\n" +
                "                    \"zh-hans\": \"314年\",\n" +
                "                    \"zh-hant\": \"314年\",\n" +
                "                    \"zh-cn\": \"314年\",\n" +
                "                    \"zh-hk\": \"314年\",\n" +
                "                    \"zh-mo\": \"314年\",\n" +
                "                    \"zh-my\": \"314年\",\n" +
                "                    \"zh-sg\": \"314年\",\n" +
                "                    \"zh-tw\": \"314年\"\n" +
                "                },\n" +
                "                \"thumbnail\": {\n" +
                "                    \"source\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Jackson_County_Alabama_Incorporated_and_Unincorporated_areas_Hollywood_Highlighted.svg/800px-Jackson_County_Alabama_Incorporated_and_Unincorporated_areas_Hollywood_Highlighted.svg.png\",\n" +
                "                    \"width\": 800,\n" +
                "                    \"height\": 471\n" +
                "                }\n" +
                "            },\n" +
                "            \"5699696\": {\n" +
                "                \"pageid\": 5699696,\n" +
                "                \"ns\": 0,\n" +
                "                \"title\": \"艺术家联合名录\",\n" +
                "                \"extract\": \"艺术家联合名录（英语：The Union List of Artist Names，缩写ULAN）是一个使用受控词表的在线数据库，目前约有293,000个艺术家的名字和其他信息。ULAN中的名称可能包括姓名、笔名、变体拼写、各语言中的名称，以及随时间变化的姓名（例如婚后姓名）。在这些名称中，其中一个被标为首选名称。\\n该名录也名：艺术家人名规范档、艺术家人名权威档、艺术家姓名联合目录、盖蒂联盟艺术家名称表。\",\n" +
                "                \"contentmodel\": \"wikitext\",\n" +
                "                \"pagelanguage\": \"zh\",\n" +
                "                \"pagelanguagehtmlcode\": \"zh\",\n" +
                "                \"pagelanguagedir\": \"ltr\",\n" +
                "                \"touched\": \"2025-03-14T16:16:51Z\",\n" +
                "                \"lastrevid\": 61748011,\n" +
                "                \"length\": 4360,\n" +
                "                \"fullurl\": \"https://zh.wikipedia.org/wiki/%E8%89%BA%E6%9C%AF%E5%AE%B6%E8%81%94%E5%90%88%E5%90%8D%E5%BD%95\",\n" +
                "                \"editurl\": \"https://zh.wikipedia.org/w/index.php?title=%E8%89%BA%E6%9C%AF%E5%AE%B6%E8%81%94%E5%90%88%E5%90%8D%E5%BD%95&action=edit\",\n" +
                "                \"canonicalurl\": \"https://zh.wikipedia.org/wiki/%E8%89%BA%E6%9C%AF%E5%AE%B6%E8%81%94%E5%90%88%E5%90%8D%E5%BD%95\",\n" +
                "                \"varianttitles\": {\n" +
                "                    \"zh\": \"艺术家联合名录\",\n" +
                "                    \"zh-hans\": \"艺术家联合名录\",\n" +
                "                    \"zh-hant\": \"藝術家聯合名錄\",\n" +
                "                    \"zh-cn\": \"艺术家联合名录\",\n" +
                "                    \"zh-hk\": \"藝術家聯合名錄\",\n" +
                "                    \"zh-mo\": \"藝術家聯合名錄\",\n" +
                "                    \"zh-my\": \"艺术家联合名录\",\n" +
                "                    \"zh-sg\": \"艺术家联合名录\",\n" +
                "                    \"zh-tw\": \"藝術家聯合名錄\"\n" +
                "                },\n" +
                "                \"thumbnail\": {\n" +
                "                    \"source\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Jackson_County_Alabama_Incorporated_and_Unincorporated_areas_Hollywood_Highlighted.svg/800px-Jackson_County_Alabama_Incorporated_and_Unincorporated_areas_Hollywood_Highlighted.svg.png\",\n" +
                "                    \"width\": 800,\n" +
                "                    \"height\": 471\n" +
                "                }\n" +
                "            },\n" +
                "            \"2779215\": {\n" +
                "                \"pageid\": 2779215,\n" +
                "                \"ns\": 0,\n" +
                "                \"title\": \"三塘乡 (余干县)\",\n" +
                "                \"extract\": \"三塘乡，是中华人民共和国江西省上饶市余干县下辖的一个乡镇级行政单位。\",\n" +
                "                \"contentmodel\": \"wikitext\",\n" +
                "                \"pagelanguage\": \"zh\",\n" +
                "                \"pagelanguagehtmlcode\": \"zh\",\n" +
                "                \"pagelanguagedir\": \"ltr\",\n" +
                "                \"touched\": \"2025-03-14T16:11:32Z\",\n" +
                "                \"lastrevid\": 72738427,\n" +
                "                \"length\": 584,\n" +
                "                \"fullurl\": \"https://zh.wikipedia.org/wiki/%E4%B8%89%E5%A1%98%E4%B9%A1_(%E4%BD%99%E5%B9%B2%E5%8E%BF)\",\n" +
                "                \"editurl\": \"https://zh.wikipedia.org/w/index.php?title=%E4%B8%89%E5%A1%98%E4%B9%A1_(%E4%BD%99%E5%B9%B2%E5%8E%BF)&action=edit\",\n" +
                "                \"canonicalurl\": \"https://zh.wikipedia.org/wiki/%E4%B8%89%E5%A1%98%E4%B9%A1_(%E4%BD%99%E5%B9%B2%E5%8E%BF)\",\n" +
                "                \"varianttitles\": {\n" +
                "                    \"zh\": \"三塘乡 (余干县)\",\n" +
                "                    \"zh-hans\": \"三塘乡 (余干县)\",\n" +
                "                    \"zh-hant\": \"三塘鄉 (餘干縣)\",\n" +
                "                    \"zh-cn\": \"三塘乡 (余干县)\",\n" +
                "                    \"zh-hk\": \"三塘鄉 (餘干縣)\",\n" +
                "                    \"zh-mo\": \"三塘鄉 (餘干縣)\",\n" +
                "                    \"zh-my\": \"三塘乡 (余干县)\",\n" +
                "                    \"zh-sg\": \"三塘乡 (余干县)\",\n" +
                "                    \"zh-tw\": \"三塘鄉 (餘干縣)\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"1622786\": {\n" +
                "                \"pageid\": 1622786,\n" +
                "                \"ns\": 0,\n" +
                "                \"title\": \"环丁四酮\",\n" +
                "                \"extract\": \"环丁四酮，也称作四氧代环丁烷，是一种假想的有机化合物，化学式为C4O4或(-(C=O)-)4。 它是一种碳的氧化物，实际上一氧化碳的四聚体。\\n这种化合物似乎在热力学上不稳定。直到2000年，它还没有被大量合成，但是通过质谱法可能检测它的存在。\",\n" +
                "                \"contentmodel\": \"wikitext\",\n" +
                "                \"pagelanguage\": \"zh\",\n" +
                "                \"pagelanguagehtmlcode\": \"zh\",\n" +
                "                \"pagelanguagedir\": \"ltr\",\n" +
                "                \"touched\": \"2025-03-14T16:10:33Z\",\n" +
                "                \"lastrevid\": 84654571,\n" +
                "                \"length\": 5412,\n" +
                "                \"fullurl\": \"https://zh.wikipedia.org/wiki/%E7%8E%AF%E4%B8%81%E5%9B%9B%E9%85%AE\",\n" +
                "                \"editurl\": \"https://zh.wikipedia.org/w/index.php?title=%E7%8E%AF%E4%B8%81%E5%9B%9B%E9%85%AE&action=edit\",\n" +
                "                \"canonicalurl\": \"https://zh.wikipedia.org/wiki/%E7%8E%AF%E4%B8%81%E5%9B%9B%E9%85%AE\",\n" +
                "                \"varianttitles\": {\n" +
                "                    \"zh\": \"环丁四酮\",\n" +
                "                    \"zh-hans\": \"环丁四酮\",\n" +
                "                    \"zh-hant\": \"環丁四酮\",\n" +
                "                    \"zh-cn\": \"环丁四酮\",\n" +
                "                    \"zh-hk\": \"環丁四酮\",\n" +
                "                    \"zh-mo\": \"環丁四酮\",\n" +
                "                    \"zh-my\": \"环丁四酮\",\n" +
                "                    \"zh-sg\": \"环丁四酮\",\n" +
                "                    \"zh-tw\": \"環丁四酮\"\n" +
                "                },\n" +
                "                \"thumbnail\": {\n" +
                "                    \"source\": \"https://upload.wikimedia.org/wikipedia/commons/1/16/Cyclobutanetetrone.png\",\n" +
                "                    \"width\": 418,\n" +
                "                    \"height\": 424\n" +
                "                }\n" +
                "            },\n" +
                "            \n" +
                "            \n" +
                "            \"1037316\": {\n" +
                "                \"pageid\": 1037316,\n" +
                "                \"ns\": 0,\n" +
                "                \"title\": \"马哈木\",\n" +
                "                \"extract\": \"马哈木（Mahamud， ？—1416年4月9日），是14、15世纪（相当中国明朝初期）蒙古汗国瓦剌部领袖。\",\n" +
                "                \"contentmodel\": \"wikitext\",\n" +
                "                \"pagelanguage\": \"zh\",\n" +
                "                \"pagelanguagehtmlcode\": \"zh\",\n" +
                "                \"pagelanguagedir\": \"ltr\",\n" +
                "                \"touched\": \"2025-03-14T16:09:27Z\",\n" +
                "                \"lastrevid\": 83384332,\n" +
                "                \"length\": 3824,\n" +
                "                \"fullurl\": \"https://zh.wikipedia.org/wiki/%E9%A9%AC%E5%93%88%E6%9C%A8\",\n" +
                "                \"editurl\": \"https://zh.wikipedia.org/w/index.php?title=%E9%A9%AC%E5%93%88%E6%9C%A8&action=edit\",\n" +
                "                \"canonicalurl\": \"https://zh.wikipedia.org/wiki/%E9%A9%AC%E5%93%88%E6%9C%A8\",\n" +
                "                \"varianttitles\": {\n" +
                "                    \"zh\": \"马哈木\",\n" +
                "                    \"zh-hans\": \"马哈木\",\n" +
                "                    \"zh-hant\": \"馬哈木\",\n" +
                "                    \"zh-cn\": \"马哈木\",\n" +
                "                    \"zh-hk\": \"馬哈木\",\n" +
                "                    \"zh-mo\": \"馬哈木\",\n" +
                "                    \"zh-my\": \"马哈木\",\n" +
                "                    \"zh-sg\": \"马哈木\",\n" +
                "                    \"zh-tw\": \"馬哈木\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"7675056\": {\n" +
                "                \"pageid\": 7675056,\n" +
                "                \"ns\": 0,\n" +
                "                \"title\": \"濠江区各级文物保护单位列表\",\n" +
                "                \"extract\": \"以下是广东省汕头市濠江区的各级文物保护单位列表。\",\n" +
                "                \"contentmodel\": \"wikitext\",\n" +
                "                \"pagelanguage\": \"zh\",\n" +
                "                \"pagelanguagehtmlcode\": \"zh\",\n" +
                "                \"pagelanguagedir\": \"ltr\",\n" +
                "                \"touched\": \"2025-01-27T02:32:29Z\",\n" +
                "                \"lastrevid\": 83555812,\n" +
                "                \"length\": 3421,\n" +
                "                \"fullurl\": \"https://zh.wikipedia.org/wiki/%E6%BF%A0%E6%B1%9F%E5%8C%BA%E5%90%84%E7%BA%A7%E6%96%87%E7%89%A9%E4%BF%9D%E6%8A%A4%E5%8D%95%E4%BD%8D%E5%88%97%E8%A1%A8\",\n" +
                "                \"editurl\": \"https://zh.wikipedia.org/w/index.php?title=%E6%BF%A0%E6%B1%9F%E5%8C%BA%E5%90%84%E7%BA%A7%E6%96%87%E7%89%A9%E4%BF%9D%E6%8A%A4%E5%8D%95%E4%BD%8D%E5%88%97%E8%A1%A8&action=edit\",\n" +
                "                \"canonicalurl\": \"https://zh.wikipedia.org/wiki/%E6%BF%A0%E6%B1%9F%E5%8C%BA%E5%90%84%E7%BA%A7%E6%96%87%E7%89%A9%E4%BF%9D%E6%8A%A4%E5%8D%95%E4%BD%8D%E5%88%97%E8%A1%A8\",\n" +
                "                \"varianttitles\": {\n" +
                "                    \"zh\": \"濠江区各级文物保护单位列表\",\n" +
                "                    \"zh-hans\": \"濠江区各级文物保护单位列表\",\n" +
                "                    \"zh-hant\": \"濠江區各級文物保護單位列表\",\n" +
                "                    \"zh-cn\": \"濠江区各级文物保护单位列表\",\n" +
                "                    \"zh-hk\": \"濠江區各級文物保護單位列表\",\n" +
                "                    \"zh-mo\": \"濠江區各級文物保護單位列表\",\n" +
                "                    \"zh-my\": \"濠江区各级文物保护单位列表\",\n" +
                "                    \"zh-sg\": \"濠江区各级文物保护单位列表\",\n" +
                "                    \"zh-tw\": \"濠江區各級文物保護單位列表\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"4562248\": {\n" +
                "                \"pageid\": 4562248,\n" +
                "                \"ns\": 0,\n" +
                "                \"title\": \"幾何化單位制\",\n" +
                "                \"extract\": \"几何化单位制（geometrized unit system），不是一种完全定义或唯一的单位制。在这单位制内，会规定光速与重力常数为1，即\\n  \\n    \\n      \\n        c\\n        =\\n        G\\n        =\\n        1\\n      \\n    \\n    {\\\\displaystyle c=G=1}\\n  \\n 。这样留出足够空间来规定其它常数，像波兹曼常数或库仑定律：\\n\\n  \\n    \\n      \\n        \\n          k\\n          \\n            B\\n          \\n        \\n        =\\n        1\\n      \\n    \\n    {\\\\displaystyle k_{B}=1}\\n  \\n、\\n\\n  \\n    \\n      \\n        \\n          \\n            1\\n            \\n              4\\n              π\\n              \\n                ϵ\\n                \\n                  0\\n                \\n              \\n            \\n          \\n        \\n        =\\n        1\\n      \\n    \\n    {\\\\displaystyle {\\\\frac {1}{4\\\\pi \\\\epsilon _{0}}}=1}\\n  \\n。\\n假若把普朗克常数也规定为 \\n  \\n    \\n      \\n        ℏ\\n        =\\n        1\\n      \\n    \\n    {\\\\displaystyle \\\\hbar =1}\\n  \\n，则几何化单位制与普朗克单位制完全相同。\\n另外，我们也可以不定义库仑常数为1，而改去定义更自然的电常数\\n  \\n    \\n      \\n        \\n          ϵ\\n          \\n            0\\n          \\n        \\n      \\n    \\n    {\\\\displaystyle \\\\epsilon _{0}}\\n  \\n为1，此时，库仑常数就会变成\\n  \\n    \\n      \\n        \\n          \\n            1\\n            \\n              4\\n              π\\n            \\n          \\n        \\n      \\n    \\n    {\\\\displaystyle {\\\\frac {1}{4\\\\pi }}}\\n  \\n，这是比较自然的有理化几何单位制，如果是定义库仑常数为1，则会是非理化的几何单位制。\",\n" +
                "                \"contentmodel\": \"wikitext\",\n" +
                "                \"pagelanguage\": \"zh\",\n" +
                "                \"pagelanguagehtmlcode\": \"zh\",\n" +
                "                \"pagelanguagedir\": \"ltr\",\n" +
                "                \"touched\": \"2025-02-17T09:57:13Z\",\n" +
                "                \"lastrevid\": 75627167,\n" +
                "                \"length\": 3678,\n" +
                "                \"fullurl\": \"https://zh.wikipedia.org/wiki/%E5%B9%BE%E4%BD%95%E5%8C%96%E5%96%AE%E4%BD%8D%E5%88%B6\",\n" +
                "                \"editurl\": \"https://zh.wikipedia.org/w/index.php?title=%E5%B9%BE%E4%BD%95%E5%8C%96%E5%96%AE%E4%BD%8D%E5%88%B6&action=edit\",\n" +
                "                \"canonicalurl\": \"https://zh.wikipedia.org/wiki/%E5%B9%BE%E4%BD%95%E5%8C%96%E5%96%AE%E4%BD%8D%E5%88%B6\",\n" +
                "                \"varianttitles\": {\n" +
                "                    \"zh\": \"幾何化單位制\",\n" +
                "                    \"zh-hans\": \"几何化单位制\",\n" +
                "                    \"zh-hant\": \"幾何化單位制\",\n" +
                "                    \"zh-cn\": \"几何化单位制\",\n" +
                "                    \"zh-hk\": \"幾何化單位制\",\n" +
                "                    \"zh-mo\": \"幾何化單位制\",\n" +
                "                    \"zh-my\": \"几何化单位制\",\n" +
                "                    \"zh-sg\": \"几何化单位制\",\n" +
                "                    \"zh-tw\": \"幾何化單位制\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"3055077\": {\n" +
                "                \"pageid\": 3055077,\n" +
                "                \"ns\": 0,\n" +
                "                \"title\": \"格拉瓦洛斯\",\n" +
                "                \"extract\": \"格拉瓦洛斯（西班牙语：Grávalos），是西班牙拉里奥哈自治区的一个市镇。总面积31平方公里，总人口274人（2001年），人口密度9人/平方公里。\",\n" +
                "                \"contentmodel\": \"wikitext\",\n" +
                "                \"pagelanguage\": \"zh\",\n" +
                "                \"pagelanguagehtmlcode\": \"zh\",\n" +
                "                \"pagelanguagedir\": \"ltr\",\n" +
                "                \"touched\": \"2025-02-17T09:56:09Z\",\n" +
                "                \"lastrevid\": 39115531,\n" +
                "                \"length\": 2215,\n" +
                "                \"fullurl\": \"https://zh.wikipedia.org/wiki/%E6%A0%BC%E6%8B%89%E7%93%A6%E6%B4%9B%E6%96%AF\",\n" +
                "                \"editurl\": \"https://zh.wikipedia.org/w/index.php?title=%E6%A0%BC%E6%8B%89%E7%93%A6%E6%B4%9B%E6%96%AF&action=edit\",\n" +
                "                \"canonicalurl\": \"https://zh.wikipedia.org/wiki/%E6%A0%BC%E6%8B%89%E7%93%A6%E6%B4%9B%E6%96%AF\",\n" +
                "                \"varianttitles\": {\n" +
                "                    \"zh\": \"格拉瓦洛斯\",\n" +
                "                    \"zh-hans\": \"格拉瓦洛斯\",\n" +
                "                    \"zh-hant\": \"格拉瓦洛斯\",\n" +
                "                    \"zh-cn\": \"格拉瓦洛斯\",\n" +
                "                    \"zh-hk\": \"格拉瓦洛斯\",\n" +
                "                    \"zh-mo\": \"格拉瓦洛斯\",\n" +
                "                    \"zh-my\": \"格拉瓦洛斯\",\n" +
                "                    \"zh-sg\": \"格拉瓦洛斯\",\n" +
                "                    \"zh-tw\": \"格拉瓦洛斯\"\n" +
                "                },\n" +
                "                \"thumbnail\": {\n" +
                "                    \"source\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Spain_location_map.svg/800px-Spain_location_map.svg.png\",\n" +
                "                    \"width\": 800,\n" +
                "                    \"height\": 686\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    },\n" +
                "    \"limits\": {\n" +
                "        \"extracts\": 20\n" +
                "    }\n" +
                "}"

        val json = Json { ignoreUnknownKeys = true }

        return json.decodeFromString<WikiApiResponse>(jsonString)
    }
}
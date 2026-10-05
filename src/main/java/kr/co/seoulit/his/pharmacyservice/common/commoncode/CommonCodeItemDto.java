package kr.co.seoulit.his.pharmacyservice.common.commoncode;

/** admin-service GET /api/admin/commonCodeItem/list?groupId= 응답 한 행 */
record CommonCodeItemDto(String codeId, String groupId, String codeValue, String codeName, String useYn) {
}

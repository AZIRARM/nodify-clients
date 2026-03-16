// params/PaginationParams.java
package io.github.AZIRARM.content.client.models;

public class PaginationParams {
    private Integer currentPage;
    private Integer limit;

    public PaginationParams() {}

    public PaginationParams(Integer currentPage, Integer limit) {
        this.currentPage = currentPage;
        this.limit = limit;
    }

    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }

    public String toQueryString() {
        StringBuilder sb = new StringBuilder();
        if (currentPage != null) {
            sb.append("currentPage=").append(currentPage);
        }
        if (limit != null) {
            if (sb.length() > 0) sb.append("&");
            sb.append("limit=").append(limit);
        }
        return sb.toString();
    }
}
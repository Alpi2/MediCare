package com.hospital.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public class PageResponse<T> {
  private List<T> content;
  private int pageNumber;
  private int pageSize;
  private long totalElements;
  private int totalPages;
  private boolean first;
  private boolean last;
  private boolean empty;

  public PageResponse() {}

  // Generic builder factory for explicit builder pattern
  public static <T> Builder<T> builder() { return new Builder<>(); }

  public static class Builder<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;

    public Builder<T> content(List<T> content) { this.content = content; return this; }
    public Builder<T> pageNumber(int pageNumber) { this.pageNumber = pageNumber; return this; }
    public Builder<T> pageSize(int pageSize) { this.pageSize = pageSize; return this; }
    public Builder<T> totalElements(long totalElements) { this.totalElements = totalElements; return this; }
    public Builder<T> totalPages(int totalPages) { this.totalPages = totalPages; return this; }
    public Builder<T> first(boolean first) { this.first = first; return this; }
    public Builder<T> last(boolean last) { this.last = last; return this; }
    public Builder<T> empty(boolean empty) { this.empty = empty; return this; }

    public PageResponse<T> build() {
      final PageResponse<T> r = new PageResponse<>();
      r.content = this.content;
      r.pageNumber = this.pageNumber;
      r.pageSize = this.pageSize;
      r.totalElements = this.totalElements;
      r.totalPages = this.totalPages;
      r.first = this.first;
      r.last = this.last;
      r.empty = this.empty;
      return r;
    }
  }

  // Explicit getters/setters
  public List<T> getContent() { return content; }
  public void setContent(List<T> content) { this.content = content; }
  public int getPageNumber() { return pageNumber; }
  public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
  public int getPageSize() { return pageSize; }
  public void setPageSize(int pageSize) { this.pageSize = pageSize; }
  public long getTotalElements() { return totalElements; }
  public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
  public int getTotalPages() { return totalPages; }
  public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
  public boolean isFirst() { return first; }
  public void setFirst(boolean first) { this.first = first; }
  public boolean isLast() { return last; }
  public void setLast(boolean last) { this.last = last; }
  public boolean isEmpty() { return empty; }
  public void setEmpty(boolean empty) { this.empty = empty; }

  public static <T> PageResponse<T> fromPage(Page<T> page) {
    return PageResponse.<T>builder()
      .content(page.getContent())
      .pageNumber(page.getNumber())
      .pageSize(page.getSize())
      .totalElements(page.getTotalElements())
      .totalPages(page.getTotalPages())
      .first(page.isFirst())
      .last(page.isLast())
      .empty(page.isEmpty())
      .build();
  }
}

require "solr_util"
class CommentsController < ApplicationController
  def index
    default_order = '(likes - dislikes) DESC, created_at ASC'
    order = ['created_at ASC', 'created_at DESC', default_order].find { |e| e == (cookies[:order] || default_order) } || default_order
    @commentable = find_commentable
    @comments = @commentable.comments.order(order)
    @comment = Comment.new
    respond_to do |format|
      format.js
    end
  end
  
  def create
    @commentable = find_commentable
    @comment = Comment.new(params[:comment])
    @comment.commentable = @commentable
    if @comment.save
      SolrUtil.index_comment(@comment)
      respond_to do |format|
        format.js do
          @id = @comment.id
          @comment = Comment.new
          @comments = @commentable.comments.order('created_at ASC')
        end
      end
    else
      respond_to do |format|
        format.js { render :action => 'new' }
      end
    end
  end
  
  def find_commentable
    params.each do |name, value|
      if name =~ /(.+)_id$/
        return $1.classify.constantize.find(value)
      end
    end
    nil
  end

end
class RootController < ApplicationController
  def index
    @top_liked = Change.where(:category => category).liked.paginate(:page => 1, :per_page => Change::PER_PAGE)
    @top_hated = Change.where(:category => category).hated.paginate(:page => 1, :per_page => Change::PER_PAGE)
    @changes = Change.where(:category => category).order('name ASC')
  end
  
  def top_liked
    @top_liked = Change.where(:category => category).liked.paginate(:page => params[:page], :per_page => Change::PER_PAGE)
    respond_to do |format|
      format.js
    end
  end
  
  def top_hated
    @top_hated = Change.where(:category => category).hated.paginate(:page => params[:page], :per_page => Change::PER_PAGE)
    respond_to do |format|
      format.js
    end
  end
end
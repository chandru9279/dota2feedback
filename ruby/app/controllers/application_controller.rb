class ApplicationController < ActionController::Base
  protect_from_forgery
  
  protected
  
  helper_method :category
  def category
    params[:category].blank? ? 'Hero' : params[:category].titleize
  end
end

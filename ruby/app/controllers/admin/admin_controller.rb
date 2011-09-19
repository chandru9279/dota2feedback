class Admin::AdminController < ApplicationController
	before_filter :authenticate
	
	def index
    @changes = Change.order('category ASC, name ASC')
		render :template => 'admin/index'
	end
	
	protected
	
	def authenticate
		authenticate_or_request_with_http_basic do |username, password|
			username == "admin" && password == "notmypassword"
		end
	end
end
class Admin::ChangesController < Admin::AdminController
  def new
    @change = Change.new
  end
  
  def create
    @change = Change.new(params[:change])
    if @change.save
      redirect_to '/admin'
    else
      render :action => 'new'
    end
  end
	
	def edit
		@change = Change.find(params[:id])
	end
	
	def update
		@change = Change.find(params[:id])
    @change.icon = nil if params[:remove_icon]
    @change.face = nil if params[:remove_face]
    @change.screenshot = nil if params[:remove_screenshot]
    @change.full_screenshot = nil if params[:remove_full_screenshot]
    if @change.update_attributes(params[:change])
      redirect_to '/admin'
    else
      render :action => 'edit'
    end
	end
	
	def destroy
		@change = Change.find(params[:id])
    @change.destroy
		redirect_to '/admin'
	end
end
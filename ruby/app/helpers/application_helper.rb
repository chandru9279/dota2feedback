module ApplicationHelper
  def breadcrumb(array, title = true)
    title((array.reverse + [['DOTA 2 Feedback', '']]).map { |e| e[0] }.join(' < ')) if title
    content_tag :p, :id => 'breadcrumb' do
      array.map { |pair| link_to(pair[0], pair[1]) }.join(' > ').html_safe
    end
  end
  
	def messages
		[:success, :notice, :info, :error, :alert].map do |key|
			message = flash[key] || instance_variable_get("@#{key}")
			content_tag :div, message, :class => key unless message.blank?
		end.join("\n").html_safe
	end
  
  def title(title)
    content_for(:title) { title.html_safe }
  end
  
  def approval(model)
    number_to_percentage(model.approval, :precision => 2)
  end
  
  def category_link(category, link)
    self.category == category ? content_tag(:span, category.pluralize) : link_to(category.pluralize, link)
  end
  
  def screenshot(change)
    # if change.full_screenshot.exists?
    if change.full_screenshot_link
      link_to image_tag(change.screenshot.url), change.full_screenshot_link, :title => 'View Screenshot in Full Size'
    else
      image_tag change.screenshot.url
    end
  end
end
